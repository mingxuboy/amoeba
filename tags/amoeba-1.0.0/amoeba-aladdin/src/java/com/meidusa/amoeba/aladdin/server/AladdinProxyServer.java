/**
 * <pre>
 *  This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 *  This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 *  You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.aladdin.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.aladdin.net.AladdinClientConnectionFactory;
import com.meidusa.amoeba.aladdin.net.AladdinClientConnectionManager;
import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.log4j.DOMConfigurator;
import com.meidusa.amoeba.mysql.context.MysqlProxyRuntimeContext;
import com.meidusa.amoeba.mysql.server.MysqlAuthenticator;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.server.IPAccessController;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.StringUtil;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * @author hexianmao
 */
public class AladdinProxyServer {

    private static Logger                log             = Logger.getLogger(AladdinProxyServer.class);
    private static Logger                repoterLog      = Logger.getLogger("report");

    private static String                name            = "Aladdin proxy Server";

    /** Used to generate "state of server" reports. */
    protected static ArrayList<Reporter> reporters       = new ArrayList<Reporter>();

    /** The time at which the server was started. */
    protected static long                serverStartTime = System.currentTimeMillis();

    /** The last time at which {@link #generateReport} was run. */
    protected static long                lastReportStamp = serverStartTime;

    /** report time interval */
    private final static long            reportInterval  = 5 * 60 * 1000L;

    protected static void registerReporter(Reporter reporter) {
        reporters.add(reporter);
    }

    /**
     * Generates a report for all system services registered as a {@link Reporter}.
     */
    protected static String generateReport() {
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

        for (int i = 0; i < reporters.size(); i++) {
            Reporter rpt = reporters.get(i);
            try {
                rpt.appendReport(report, now, sinceLast, reset, repoterLog.getLevel());
            } catch (Throwable t) {
                log.error("Reporter choked [rptr=" + rpt + "].", t);
            }
        }

        // only reset the last report time if this is a periodic report
        if (reset) {
            lastReportStamp = now;
        }

        return report.toString();
    }

    protected static void logReport() {
        new Thread() {

            {
                this.setDaemon(true);
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(reportInterval);
                    } catch (InterruptedException e) {
                    }
                    if (repoterLog.isInfoEnabled()) {
                        repoterLog.info(generateReport());
                    }
                }
            }
        }.start();
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String log4jConf = System.getProperty("log4j.conf", "${amoeba.home}/conf/log4j.xml");
        log4jConf = ConfigUtil.filter(log4jConf);
        File logconf = new File(log4jConf);
        if (logconf.exists() && logconf.isFile()) {
            DOMConfigurator.configureAndWatch(logconf.getAbsolutePath(), System.getProperties());
        }
        Logger logger = Logger.getLogger(AladdinProxyServer.class);

        String config = System.getProperty("amoeba.conf", "${amoeba.home}/conf/amoeba.xml");
        config = ConfigUtil.filter(config);
        File configFile = new File(config);
        if (config == null || !configFile.exists()) {
            logger.error("could not find config file:" + configFile.getAbsolutePath());
            System.exit(-1);
        }

        ProxyRuntimeContext context = new MysqlProxyRuntimeContext();
        ProxyRuntimeContext.getInstance().init(configFile.getAbsolutePath());
        registerReporter(context);
        for (ConnectionManager connMgr : context.getConnectionManagerList().values()) {
            registerReporter(connMgr);
        }

        AladdinClientConnectionManager aladdin = new AladdinClientConnectionManager(name,
                                                                                    context.getConfig().getIpAddress(),
                                                                                    context.getConfig().getPort());
        registerReporter(aladdin);
        AladdinClientConnectionFactory factory = new AladdinClientConnectionFactory();
        factory.setPassword(context.getConfig().getPassword());
        factory.setUser(context.getConfig().getUser());
        aladdin.setConnectionFactory(factory);
        factory.setConnectionManager(aladdin);

        MysqlAuthenticator auth = new MysqlAuthenticator();
        String accessConf = System.getProperty("access.conf", "${amoeba.home}/conf/access_list.conf");
        accessConf = ConfigUtil.filter(accessConf);
        IPAccessController ipfilter = new IPAccessController();
        ipfilter.setIpFile(accessConf);
        try {
            ipfilter.init();
        } catch (InitialisationException e1) {
            logger.error("init IPAccessController error:", e1);
            System.exit(-1);
        }
        auth.setFilter(ipfilter);

        aladdin.setAuthenticator(auth);
        aladdin.setExecutor(context.getReadExecutor());
        aladdin.start();

        logReport();
    }

}
