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

import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.log4j.DOMConfigurator;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionObserver;
import com.meidusa.amoeba.net.MultiConnectionManagerWrapper;
import com.meidusa.amoeba.util.CmdLineParser;
import com.meidusa.amoeba.util.ObjectMapLoader;
import com.meidusa.amoeba.util.StringUtil;
import com.meidusa.amoeba.util.CmdLineParser.BooleanOption;
import com.meidusa.amoeba.util.CmdLineParser.IntegerOption;
import com.meidusa.amoeba.util.CmdLineParser.LongOption;
import com.meidusa.amoeba.util.CmdLineParser.StringOption;

@SuppressWarnings("unchecked")
public abstract class AbstractBenchmark {
	private static AbstractBenchmark benckmark;
	protected static void setBenchmark(AbstractBenchmark benckmark){
		AbstractBenchmark.benckmark = benckmark;
	}
	
	private static Properties properties = new Properties();
	protected static CmdLineParser parser = new CmdLineParser(System.getProperty("application", "benchmark"));
	protected static CmdLineParser.Option debugOption = parser.addOption(new BooleanOption('d', "debug", false,false,true,"show the interaction with the server-side information"));
	protected static CmdLineParser.Option portOption = parser.addOption(new IntegerOption('p', "port",true,true,"server port"));
	protected static CmdLineParser.Option hostOption = parser.addOption(new StringOption('h', "host",true,true,"server host","127.0.0.1"));
	protected static CmdLineParser.Option connOption = parser.addOption(new IntegerOption('c', "conn",true,true,"The number of concurrent connections"));
	protected static CmdLineParser.Option totalOption = parser.addOption(new LongOption('n', "total",true,true,"total requests"));
	protected static CmdLineParser.Option timeoutOption = parser.addOption(new IntegerOption('t', "timeout",true,false,-1,"query timeout, default value=-1 "));
    
	protected static CmdLineParser.Option contextOption = parser.addOption(new StringOption('C', "context",true,false,"Context xml File"));
	protected static CmdLineParser.Option requestOption = parser.addOption(new StringOption('f', "file",true,true,"request xml File"));
	
	protected static CmdLineParser.Option splitOption = parser.addOption(new StringOption('s', "split",true,false,null,"split char"));
	
	protected static CmdLineParser.Option log4jOption = parser.addOption(new StringOption('l', "log4j",true,false,"warn","log4j level[debug,info,warn,error]"));
	
	protected static CmdLineParser.Option helpOption = parser.addOption(new BooleanOption('?', "help",false,false,true,"Show this help message"));
	private static String split = null;
	private static Map<String,RandomData> randomMap = new HashMap<String,RandomData>();
	private static Map contextMap = new HashMap(){
		public Object get(Object key){
			Object value =super.get(key);
			if(value instanceof RandomData){
				String line = (String)((RandomData) value).nextData();
				if(split == null){
					return StringUtil.split(line);
				}else{
					return StringUtil.split(line,split);
				}
			}
			return value;
		}
		
		public Object put(Object key,Object value){
			if(value instanceof RandomData){
				randomMap.put((String)key, (RandomData)value);
			}
			super.put(key, value);
			return value;
		}
	};
	public AbstractBenchmark(){
		Random random = new Random();
		contextMap.put("random",random);
		contextMap.put("atomicInteger",new AtomicInteger());
		contextMap.put("atomicLong",new AtomicLong());
		
		String requestXml = (String)parser.getOptionValue(requestOption);
		if(requestXml != null){
			File reqestXmlFile = new File(requestXml);
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
			System.err.println("--file="+requestXml+" not found");
			System.exit(-1);
		}
		
		String contextFile = (String)parser.getOptionValue(contextOption);
		
		if(contextFile != null){
			File contextXmlFile = new File(contextFile);
			if(contextXmlFile.exists() && contextXmlFile.isFile()){
				try {
					ObjectMapLoader.load(contextMap,new FileInputStream(contextXmlFile));
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
	
	
	public Map getNextRequestContextMap(){
		for(Map.Entry<String, RandomData> entry : randomMap.entrySet()){
			String line = (String)entry.getValue().nextData();
			Object obj = null;
			if(split == null){
				obj = StringUtil.split(line);
			}else{
				obj = StringUtil.split(line,split);
			}
			contextMap.put(entry.getKey(), obj);
		}
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
		
		String level = (String)parser.getOptionValue(log4jOption);
		if(level != null){
			System.setProperty("benchmark.level", level);
		}else{
			System.setProperty("benchmark.level", "warn");
		}
		
		split = (String)parser.getOptionValue(splitOption);
		
		final Boolean value = (Boolean)parser.getOptionValue(debugOption,false);
		
		String log4jConf = System.getProperty("log4j.conf","${amoeba.home}/conf/log4j.xml");
		log4jConf = ConfigUtil.filter(log4jConf);
		File logconf = new File(log4jConf);
		if(logconf.exists() && logconf.isFile()){
			DOMConfigurator.configureAndWatch(logconf.getAbsolutePath(), System.getProperties());
		}

		int conn = (Integer)parser.getOptionValue(connOption);
		final long total = (Long)parser.getOptionValue(totalOption);
		String ip = parser.getOptionValue(hostOption).toString();
		
		final CountDownLatch requestLatcher = new CountDownLatch((int)total);
		final CountDownLatch responseLatcher = new CountDownLatch((int)total);
		final TaskRunnable task = new TaskRunnable();
		int port = (Integer)parser.getOptionValue(portOption);
		
		final MultiConnectionManagerWrapper manager = new MultiConnectionManagerWrapper();
		manager.addConnectionObserver(new ConnectionObserver(){

			@Override
			public void connectionClosed(Connection conn) {
				if(value.booleanValue()){
					System.out.println(new Date() +"     client conn="+conn.getSocketId()+" closed!");
				}
			}

			@Override
			public void connectionEstablished(Connection conn) {
				if(value.booleanValue()){
					System.out.println(new Date() +"     client conn="+conn.getSocketId()+" connected!");
				}
				
			}

			@Override
			public void connectionFailed(Connection conn, Exception fault) {
				if(value.booleanValue()){
					System.out.println(new Date() +"     client conn="+conn.getSocketId()+ " faild!! "+(fault!= null? (" fault="+fault.getMessage()):""));
				}
			}
			
		});
		Integer timeout = (Integer)parser.getOptionValue(timeoutOption,-1);
		if(timeout >0){
			manager.setIdleCheckTime(timeout);
		}
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
					System.out.println(new Date() +"     compeleted="+(total - lastCount)+ " TPS="+tps +" ,conns="+manager.getSize());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				System.out.println(new Date() +"     compeleted="+(total));
			}
			
		}.start();
		
		System.out.println("\r\nconnect to ip="+ip+",port="+port+",connection size="+conn+",total request="+total);
		AbstractBenchmark benckmark = AbstractBenchmark.getInstance();
		List<AbstractBenchmarkClientConnection<?>> connList = new ArrayList<AbstractBenchmarkClientConnection<?>>();
		for(int i=0;i<conn;i++){
			InetSocketAddress address = new InetSocketAddress(ip,port);
			try{
				AbstractBenchmarkClientConnection<?> connection = benckmark.newBenchmarkClientConnection(SocketChannel.open(address),System.currentTimeMillis(),requestLatcher,responseLatcher,task);
				connection.setBenchmark(benckmark);
				connection.setTimeout(timeout.intValue());
				connection.setDebug(value.booleanValue());
				
				connection.putAllRequestProperties(properties);
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
		average = cost / total;
		long time = TimeUnit.MILLISECONDS.convert((maxend - minStart),TimeUnit.NANOSECONDS);
		System.out.println("completed requests total="+total+", cost="+TimeUnit.MILLISECONDS.convert((maxend - minStart), TimeUnit.NANOSECONDS)+"ms , TPS="+ (time>0?((long)total*1000)/time:total)+"/s");
		System.out.println("min="+TimeUnit.MILLISECONDS.convert(min, TimeUnit.NANOSECONDS)+"ms");
		System.out.println("max="+TimeUnit.MILLISECONDS.convert(max, TimeUnit.NANOSECONDS)+"ms");
		System.out.println("average="+TimeUnit.MILLISECONDS.convert(average, TimeUnit.NANOSECONDS)+"ms");
		manager.shutdown();
		
	}
}
