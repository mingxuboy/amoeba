package com.meidusa.amoeba.benchmark;


import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.MultiConnectionManagerWrapper;
import com.meidusa.amoeba.util.CmdLineParser;
import com.meidusa.amoeba.util.OptionType;

@SuppressWarnings("unchecked")
public abstract class AbstractBenchmark {
	private static AbstractBenchmark benckmark;
	protected static void setBenchmark(AbstractBenchmark benckmark){
		AbstractBenchmark.benckmark = benckmark;
	}
	
	private static Map contextMap = new HashMap();
	private static Properties properties = new Properties();
	public AbstractBenchmark(){
		Random random = new Random();
		contextMap.put("random",random);
		contextMap.put("atomicInteger",new AtomicInteger());
		contextMap.put("atomicLong",new AtomicLong());
		String reqestXml = System.getProperty("requestFile");
		
		if(reqestXml != null){
			File reqestXmlFile = new File(reqestXml);
			if(reqestXmlFile.exists() && reqestXmlFile.isFile()){
				try {
					properties.loadFromXML(new FileInputStream(reqestXmlFile));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}else{
				System.err.println("requestFile not found or is not file :"+reqestXmlFile.getAbsolutePath());
				System.exit(-1);
			}
		}else{
			System.err.println("system property named 'requestFile' not set");
			System.exit(-1);
		}
		
		String contextFile = System.getProperty("contextFile");
		
		if(contextFile != null){
			Properties properties = new Properties();
			File contextXmlFile = new File(contextFile);
			if(contextXmlFile.exists() && contextXmlFile.isFile()){
				try {
					properties.loadFromXML(new FileInputStream(contextXmlFile));
					for(Map.Entry entry : properties.entrySet()){
						String name = (String)entry.getKey();
						Object obj = Class.forName(((String)entry.getValue()).trim()).newInstance();
						contextMap.put(name, obj);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}else{
				System.err.println("requestFile not found or not file :"+contextXmlFile.getAbsolutePath());
				System.exit(-1);
			}
		}
		
	}
	
	
	public Map getContextMap(){
		return contextMap;
	}
	
	public static AbstractBenchmark getInstance(){
		return AbstractBenchmark.benckmark;
	}
	public abstract AbstractBenchmarkClientConnection<?> newBenchmarkClientConnection(SocketChannel channel,long time,CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task);
	public static class TaskRunnable{
		public boolean running = true;
	}
	public static void main(String[] args) throws Exception {
		CmdLineParser parser = new CmdLineParser(System.getProperty("application", "BenchMark"));
    	CmdLineParser.Option debugOption = parser.addOption(OptionType.Boolean,'d', "debug", false,"show the interaction with the server-side information");
        CmdLineParser.Option portOption = parser.addOption(OptionType.String,'p', "port",true,"server port");
        CmdLineParser.Option hostOption = parser.addOption(OptionType.String,'h', "host",true,"server host");
        CmdLineParser.Option connOption = parser.addOption(OptionType.String,'c', "conn",true,"The number of concurrent connections");
        CmdLineParser.Option totleOption = parser.addOption(OptionType.Long,'t', "totle",true,"totle requests");
        CmdLineParser.Option helpOption = parser.addOption(OptionType.String,'?', "help",false,"Show this help message");
        
        try {
            parser.parse(args);
            Boolean value = (Boolean)parser.getOptionValue(helpOption);
        	if(value != null && value.booleanValue()){
        		parser.printUsage();
        		System.exit(2);
        	}
        }catch ( CmdLineParser.OptionException e ) {
        	Boolean value = (Boolean)parser.getOptionValue(helpOption);
        	if(value != null && value.booleanValue()){
        		parser.printUsage();
        	}else{
        		System.err.println(e.getMessage());
            	parser.printUsage();
        	}
        	System.exit(2);
        }
        
		Logger logger = Logger.getLogger("rootLogger");
		logger.addAppender(new ConsoleAppender());
		logger.setLevel(Level.DEBUG);
		if(args != null && args.length ==1 && "-h".equalsIgnoreCase(args[0])){
			System.out.println("-Dconn=<int> ;Concurrency connection size\r\n");
			System.out.println("-Dip=<String> ;remote ip\r\n");
			System.out.println("-Dport=<int> ;remote port\r\n");
			System.out.println("-Dtotle=<int> ;totle request\r\n");
			System.out.println("-Ddebug=<Boolean> ;enable debug\r\n");
			return;
		}

		int conn = (Integer)parser.getOptionValue(connOption);
		final int totle = (Integer)parser.getOptionValue(totleOption);
		String ip = parser.getOptionValue(hostOption).toString();
		
		final CountDownLatch requestLatcher = new CountDownLatch(totle);
		final CountDownLatch responseLatcher = new CountDownLatch(totle);
		final TaskRunnable task = new TaskRunnable();
		int port = (Integer)parser.getOptionValue(portOption);
		
		MultiConnectionManagerWrapper manager = new MultiConnectionManagerWrapper();
		manager.init();
		manager.start();
		Thread.sleep(100L);
		System.out.println("Connection manager started....");
		new Thread(){
			long lastCount = responseLatcher.getCount();
			{this.setDaemon(true);}
			public void run(){
				while(responseLatcher.getCount()>0){
					long current = responseLatcher.getCount();
					long tps = lastCount - current;
					lastCount = current;
					System.out.println(new Date() +"     compeleted="+(totle - lastCount)+ " TPS="+tps);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				System.out.println(new Date() +"     compeleted="+(totle));
			}
			
		}.start();
		
		System.out.println("\r\nconnect to ip="+ip+",port="+port+",connection size="+conn+",totle request="+totle);
		AbstractBenchmark benckmark = AbstractBenchmark.getInstance();
		List<AbstractBenchmarkClientConnection<?>> connList = new ArrayList<AbstractBenchmarkClientConnection<?>>();
		for(int i=0;i<conn;i++){
			InetSocketAddress address = new InetSocketAddress(ip,port);
			try{
				AbstractBenchmarkClientConnection<?> connection = benckmark.newBenchmarkClientConnection(SocketChannel.open(address),System.currentTimeMillis(),requestLatcher,responseLatcher,task);
				connection.putAllRequestProperties(properties);
				connection.setContextMap(benckmark.getContextMap());
				manager.postRegisterNetEventHandler(connection, SelectionKey.OP_READ);
				connList.add(connection);
			}catch(Exception e){
				System.err.println("connect to "+address+" error:");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		
		for(AbstractBenchmarkClientConnection<?> connection: connList){
			if(requestLatcher.getCount()>0){
				requestLatcher.countDown();
				connection.startBenchmark();
			}
		}
		requestLatcher.await();
		task.running = false;
		responseLatcher.await();
		
		long min = connList.get(0).min;
		long max = 0;
		long average = 0;
		long cost = 0;
		long count = 0;
		long minStart = connList.get(0).start;
		long maxend = 0;
		for(AbstractBenchmarkClientConnection<?> connection: connList){
			if(connection.count>0){
				min = Math.min(min, connection.min);
				max = Math.max(max, connection.max);
				cost += (connection.end - connection.start);
				count += connection.count;
				minStart = Math.min(minStart,connection.start);
				maxend = Math.max(maxend, connection.end);
			}
		}
		average = cost / totle;
		long time = TimeUnit.MILLISECONDS.convert((maxend - minStart),TimeUnit.NANOSECONDS);
		System.out.println("completed requests totle="+totle+", cost="+TimeUnit.MILLISECONDS.convert((maxend - minStart), TimeUnit.NANOSECONDS)+"ms , TPS="+ (time>0?((long)totle*1000)/time:totle)+"/s");
		System.out.println("min="+TimeUnit.MILLISECONDS.convert(min, TimeUnit.NANOSECONDS)+"ms");
		System.out.println("max="+TimeUnit.MILLISECONDS.convert(max, TimeUnit.NANOSECONDS)+"ms");
		System.out.println("average="+TimeUnit.MILLISECONDS.convert(average, TimeUnit.NANOSECONDS)+"ms");
		manager.shutdown();
		
	}
}
