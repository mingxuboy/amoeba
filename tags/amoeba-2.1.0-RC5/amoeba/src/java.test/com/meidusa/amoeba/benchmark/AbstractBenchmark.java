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
import com.meidusa.amoeba.net.AuthingableConnection;
import com.meidusa.amoeba.net.BackendConnectionFactory;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionFactory;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.net.ConnectionObserver;
import com.meidusa.amoeba.net.MultiConnectionManagerWrapper;
import com.meidusa.amoeba.util.CmdLineParser;
import com.meidusa.amoeba.util.ObjectMapLoader;
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
	protected static CmdLineParser.Option portOption = parser.addOption(new IntegerOption('P', "port",true,true,"server port"));
	protected static CmdLineParser.Option hostOption = parser.addOption(new StringOption('h', "host",true,true,"127.0.0.1","server host"));
	protected static CmdLineParser.Option connOption = parser.addOption(new IntegerOption('c', "conn",true,true,"The number of concurrent connections"));
	protected static CmdLineParser.Option totalOption = parser.addOption(new LongOption('n', "total",true,true,"total requests"));
	protected static CmdLineParser.Option timeoutOption = parser.addOption(new IntegerOption('t', "timeout",true,false,-1,"query timeout, default value=-1 "));
    
	protected static CmdLineParser.Option contextOption = parser.addOption(new StringOption('C', "context",true,false,"Context xml File"));
	protected static CmdLineParser.Option requestOption = parser.addOption(new StringOption('f', "file",true,false,"request xml File"));
	
	protected static CmdLineParser.Option log4jOption = parser.addOption(new StringOption('l', "log4j",true,false,"warn","log4j level[debug,info,warn,error]"));
	
	protected static CmdLineParser.Option helpOption = parser.addOption(new BooleanOption('?', "help",false,false,true,"Show this help message"));
	private static Map<String,RandomData> randomMap = new HashMap<String,RandomData>();
	private static Map contextMap = new HashMap(){

		public Object put(Object key,Object value){
			if(value instanceof RandomData){
				randomMap.put((String)key, (RandomData)value);
			}
			super.put(key, value);
			return value;
		}
	};
	
	private List<AbstractBenchmarkClient<?>> benchmarkClientList = new ArrayList<AbstractBenchmarkClient<?>>();
	
	
	public List<AbstractBenchmarkClient<?>> getBenchmarkClientList() {
		return benchmarkClientList;
	}

	public CmdLineParser getCmdLineParser(){
		return parser;
	}
	
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
	
	public abstract ConnectionFactory getConnectionFactory();
	
	private ConnectionManager connManager; 
	
	public ConnectionManager getConnManager() {
		return connManager;
	}

	public void setConnManager(ConnectionManager connManager) {
		this.connManager = connManager;
	}

	public Map getNextRequestContextMap(){
		Map temp = new HashMap();
		temp.putAll(contextMap);
		for(Map.Entry<String, RandomData> entry : randomMap.entrySet()){
			Object obj = null;
			do{
				obj = entry.getValue().nextData();
			}while(obj == null);
			temp.put(entry.getKey(), obj);
		}
		return temp;
	}
	
	public static AbstractBenchmark getInstance(){
		return AbstractBenchmark.benckmark;
	}
	
	public abstract AbstractBenchmarkClient<?> newBenchmarkClient(Connection conn,CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task);
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
		final AtomicLong errorNum = new AtomicLong(0);
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
				
				if(conn instanceof AuthingableConnection){
					AuthingableConnection authConn = (AuthingableConnection)conn;
					if(authConn.isAuthenticatedSeted() && authConn.isAuthenticated()){
						errorNum.incrementAndGet();
					}
				}else{
					errorNum.incrementAndGet();
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
			long lastTime = System.currentTimeMillis();
			{this.setDaemon(true);}
			public void run(){
				
				while(responseLatcher.getCount()>0){
					long current = responseLatcher.getCount();
					long currentTime = System.currentTimeMillis();
					long tps = 0;
					if(currentTime > lastTime){
						tps = (lastCount - current) * 1000 /(currentTime-lastTime);
					}else{
						tps = (lastCount - current);
					}
					lastCount = current;
					lastTime = currentTime;
					System.out.println(new Date() +"     compeleted="+(total - lastCount)+ " TPS="+tps +" ,conns="+manager.getSize());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					
					if(requestLatcher.getCount() == 0){
						if( responseLatcher.getCount() - errorNum.get() <=0){
							break;
						}
					}
				}
				
				for(long i=0;i<errorNum.get();i++){
					responseLatcher.countDown();
				}
				
				System.out.println(new Date() +"     compeleted="+(total));
			}
			
		}.start();
		
		System.out.println("\r\nconnect to ip="+ip+",port="+port+",connection size="+conn+",total request="+total);
		AbstractBenchmark benckmark = AbstractBenchmark.getInstance();
		benckmark.setConnManager(manager);
		ConnectionFactory factory = benckmark.getConnectionFactory();
		if(factory instanceof BackendConnectionFactory)
		{
			((BackendConnectionFactory)factory).setManager(manager);
			((BackendConnectionFactory)factory).setIpAddress(ip);
			((BackendConnectionFactory)factory).setPort(port);
			((BackendConnectionFactory)factory).init();
		}
		
		long createConnectionStartTime = System.nanoTime();
		for(int i=0;i<conn;i++){
			InetSocketAddress address = new InetSocketAddress(ip,port);
			try{
				Connection connection = factory.createConnection(SocketChannel.open(address),System.currentTimeMillis());
				
				AbstractBenchmarkClient<?> client = benckmark.newBenchmarkClient(connection,requestLatcher,responseLatcher,task);
				client.setBenchmark(benckmark);
				client.setTimeout(timeout.intValue());
				client.setDebug(value.booleanValue());
				
				client.putAllRequestProperties(properties);
				client.init();
				if(!(factory instanceof BackendConnectionFactory)){
					manager.postRegisterNetEventHandler(client.getConnection(), SelectionKey.OP_READ);
				}
				benckmark.benchmarkClientList.add(client);
			}catch(Exception e){
				System.err.println("connect to "+address+" error:");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		long createConnectionEndTime = System.nanoTime();
		
		for(AbstractBenchmarkClient<?> connection: benckmark.benchmarkClientList){
			if(requestLatcher.getCount()>0){
				requestLatcher.countDown();
				connection.startBenchmark();
			}
		}
		
		requestLatcher.await();
		task.running = false;
		responseLatcher.await();
		long endBenchmarkTime = System.nanoTime();
		long min = benckmark.benchmarkClientList.get(0).min;
		long max = 0;
		long minStart = benckmark.benchmarkClientList.get(0).start;
		long maxend = 0;
		long average = 0;
		int totleConnection = 0;
		for(AbstractBenchmarkClient<?> connection: benckmark.benchmarkClientList){
			if(connection.count>0){
				min = Math.min(min, connection.min);
				max = Math.max(max, connection.max);
				average += (connection.end - connection.start)/connection.count;
				minStart = Math.min(minStart,connection.start);
				maxend = Math.max(maxend, connection.end);
				totleConnection ++;
			}
		}
		long time = TimeUnit.MILLISECONDS.convert((maxend - minStart),TimeUnit.NANOSECONDS);
		System.out.println("completed requests total="+total+ ", errorNum="+errorNum.get()+", cost="+TimeUnit.MILLISECONDS.convert((maxend - minStart), TimeUnit.NANOSECONDS)+"ms , TPS="+ (time>0?((long)total*1000)/time:total)+"/s");
		System.out.println("min="+TimeUnit.MILLISECONDS.convert(min, TimeUnit.NANOSECONDS)+"ms");
		System.out.println("max="+TimeUnit.MILLISECONDS.convert(max, TimeUnit.NANOSECONDS)+"ms");
		average = TimeUnit.MILLISECONDS.convert(average, TimeUnit.NANOSECONDS)/totleConnection;
		System.out.println("average="+average+"ms");
		
		System.out.println("create Connections time="+TimeUnit.MILLISECONDS.convert(createConnectionEndTime - createConnectionStartTime, TimeUnit.NANOSECONDS)+"ms");
		long tpsTime = TimeUnit.MILLISECONDS.convert(endBenchmarkTime - createConnectionEndTime, TimeUnit.NANOSECONDS);
		System.out.println("TPS(after connected)="+(tpsTime>0?((long)total*1000)/tpsTime:total)+"/s");
		manager.shutdown();
		
	}
}
