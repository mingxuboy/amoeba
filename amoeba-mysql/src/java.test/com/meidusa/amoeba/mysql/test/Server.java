package com.meidusa.amoeba.mysql.test;

import java.io.File;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.log4j.DOMConfigurator;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ServerableConnectionManager;

public class Server {
	
	public static void main(String[] args) throws Exception{
		String log4jConf = System.getProperty("log4j.conf","${amoeba.home}/conf/log4j.xml");
		log4jConf = ConfigUtil.filter(log4jConf);
		File logconf = new File(log4jConf);
		if(logconf.exists() && logconf.isFile()){
			DOMConfigurator.configureAndWatch(logconf.getAbsolutePath(), System.getProperties());
		}
		ServerableConnectionManager manager = new ServerableConnectionManager("Test Server",null,10004){
			protected void configConnection(Connection connection) throws SocketException{
				connection.getChannel().socket().setTcpNoDelay(true);
			}
		};
		class ReNameableThreadExecutor extends ThreadPoolExecutor{
			//Map<Thread,String> threadNameMap = new HashMap<Thread,String>();
			public ReNameableThreadExecutor(int poolSize) {
				super(poolSize, poolSize, Long.MAX_VALUE, TimeUnit.NANOSECONDS,new LinkedBlockingQueue<Runnable>());
			}
			
			/*protected void beforeExecute(Thread t, Runnable r) {
				if(r instanceof NameableRunner){
					NameableRunner nameableRunner = (NameableRunner)r;
					String name = t.getName();
					if(name != null){
						threadNameMap.put(t, t.getName());
						t.setName(nameableRunner.getRunnerName()+":"+t.getName());
					}
				}
			};
			protected void afterExecute(Runnable r, Throwable t) { 
				if(r instanceof NameableRunner){
					String name = threadNameMap.remove(Thread.currentThread());
					if(name != null){
						Thread.currentThread().setName(name);
					}
				}
			};*/
			
		}
		manager.setDaemon(false);
		TestConnectionFactory connFactory = new TestConnectionFactory();
		connFactory.setConnectionManager(manager);
		manager.setExecutor(new ReNameableThreadExecutor(5));
		manager.setConnectionFactory(connFactory);
		manager.start();
	}
}
