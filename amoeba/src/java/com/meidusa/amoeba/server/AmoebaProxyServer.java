/*
 * Copyright amoeba.meidusa.com
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.log4j.DOMConfigurator;
import com.meidusa.amoeba.mongodb.context.MongodbProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnectionFactory;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.net.MultiConnectionManagerWrapper;
import com.meidusa.amoeba.net.ServerableConnectionManager;
import com.meidusa.amoeba.server.IPAccessController;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * 
 */
public class AmoebaProxyServer {
	private static Logger log = Logger.getLogger(AmoebaProxyServer.class);
	private static Logger repoterLog = Logger.getLogger("report");
	/** Used to generate "state of server" reports. */
	protected static ArrayList<Reporter> reporters = new ArrayList<Reporter>();
	/** The time at which the server was started. */
	protected static long serverStartTime = System.currentTimeMillis();

	/** The last time at which {@link #generateReport} was run. */
	protected static long lastReportStamp = serverStartTime;

	public static void registerReporter(Reporter reporter) {
		reporters.add(reporter);
	}

	/**
	 * Generates a report for all system services registered as a
	 * {@link Reporter}.
	 */
	public static String generateReport() {
		return generateReport(System.currentTimeMillis(), false);
	}

	/**
	 * Generates and logs a "state of server" report.
	 */
	protected static String generateReport(long now, boolean reset) {
		long sinceLast = now - lastReportStamp;
		long uptime = now - serverStartTime;
		StringBuilder report = new StringBuilder(" State of server report:\n");

		report.append("- Uptime: ");
		report.append(StringUtil.intervalToString(uptime)).append("\n");
		report.append("- Report period: ");
		report.append(StringUtil.intervalToString(sinceLast)).append("\n");

		// report on the state of memory
		Runtime rt = Runtime.getRuntime();
		long total = rt.totalMemory(), max = rt.maxMemory();
		long used = (total - rt.freeMemory());
		report.append("- Memory: ").append(used / 1024).append("k used, ");
		report.append(total / 1024).append("k total, ");
		report.append(max / 1024).append("k max\n");
		
		for (int ii = 0; ii < reporters.size(); ii++) {
			Reporter rptr = reporters.get(ii);
			try {
				rptr.appendReport(report, now, sinceLast, reset,repoterLog.getLevel());
			} catch (Throwable t) {
				log.error("Reporter choked [rptr=" + rptr + "].", t);
			}
		}

		// only reset the last report time if this is a periodic report
		if (reset) {
			lastReportStamp = now;
		}

		return report.toString();
	}

	protected static void logReport(String report) {
		repoterLog.info(report);
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws Exception 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws Exception {
		String log4jConf = System.getProperty("log4j.conf","${amoeba.home}/conf/log4j.xml");
		log4jConf = ConfigUtil.filter(log4jConf);
		File logconf = new File(log4jConf);
		if(logconf.exists() && logconf.isFile()){
			DOMConfigurator.configureAndWatch(logconf.getAbsolutePath(), System.getProperties());
		}
		
		final Logger logger = Logger.getLogger(AmoebaProxyServer.class);
		String config = System.getProperty("amoeba.conf","${amoeba.home}/conf/amoeba.xml");
		String contextClass = System.getProperty("amoeba.context.class",ProxyRuntimeContext.class.getName());
		ProxyRuntimeContext context = (ProxyRuntimeContext)Class.forName(contextClass).newInstance();
		
		config = ConfigUtil.filter(config);
		File configFile = new File(config);
		
		if(config == null || !configFile.exists()){
			logger.error("could not find config file:"+configFile.getAbsolutePath());
			System.exit(-1);
		}else{
			ProxyRuntimeContext.getInstance().init(configFile.getAbsolutePath());
		}
		
		registerReporter(context);
		for(ConnectionManager connMgr :context.getConnectionManagerList().values()){
			registerReporter(connMgr);
		}
		
		
		String accessConf = System.getProperty("access.conf","${amoeba.home}/conf/access_list.conf");
		accessConf = ConfigUtil.filter(accessConf);
		IPAccessController ipfilter = new IPAccessController();
		ipfilter.setIpFile(accessConf);
		try {
			ipfilter.init();
		} catch (InitialisationException e1) {
			logger.error("init IPAccessController error:",e1);
			System.exit(-1);
		}
		
		
		
        int maxsubManager = Integer.valueOf(System.getProperty("maxSubManager","32"));
        int processors = Runtime.getRuntime().availableProcessors();
        ConnectionManager[] managers = new ConnectionManager[processors>=maxsubManager?maxsubManager:processors+1];
        for(int i=0;i<managers.length;i++){
        	ConnectionManager subManager = new ConnectionManager("sub manager-"+i);
    		registerReporter(subManager);
    		subManager.start();
	        managers[i] = subManager;
        }
        MultiConnectionManagerWrapper wrapper = new MultiConnectionManagerWrapper(managers);
        wrapper.start();
		ServerableConnectionManager serverManager = new ServerableConnectionManager("Mongodb proxy Server",context.getConfig().getIpAddress(),context.getConfig().getPort(),wrapper);
		
		registerReporter(serverManager);
		MongodbClientConnectionFactory factory = new MongodbClientConnectionFactory();
		serverManager.setConnectionFactory(factory);
		
		serverManager.start();
		new Thread(){
			{
				this.setDaemon(true);
			}
			public void run(){
				while(true){
					try {
						Thread.sleep(60*1000);
					} catch (InterruptedException e) {
					}
					try{
					logReport(generateReport());
					}catch(Exception e){
						logger.error("report error",e);
					}
				}
			}
		}.start();
	}
}
