package com.meidusa.amoeba.benchmark;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.benchmark.AbstractBenchmark.TaskRunnable;
import com.meidusa.amoeba.net.AuthingableConnection;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.packet.Packet;

public abstract class AbstractBenchmarkClient<T extends Packet> implements MessageHandler{
	private static Logger       logger        = Logger.getLogger(AbstractBenchmarkClient.class);
	private boolean debug = false;
	private int timeout = -1;
	private Properties properties;
	long min = System.nanoTime();
	long start = 0;
	long max = 0;
	long end = min;
	long next = min;
	long count = 0;
	protected CountDownLatch requestLatcher;
	protected CountDownLatch responseLatcher;
	protected TaskRunnable task;
	private AbstractBenchmark benchmark;
	private Connection connection;
	private MessageHandler connOldMessageHandler = null;
	public AbstractBenchmark getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(AbstractBenchmark benchmark) {
		this.benchmark = benchmark;
	}

	public Connection getConnection() {
		return connection;
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void putAllRequestProperties(Map source){
		if(properties == null){
			properties = new Properties();
		}
		properties.putAll(source);
	}
	
	public  Properties getRequestProperties(){
		return properties;
	}
	
	public AbstractBenchmarkClient(Connection connection, CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task) {
		this.connection = connection;
		start = System.nanoTime();
		this.requestLatcher = requestLatcher;
		this.responseLatcher = responseLatcher;
		this.task = task;
		connOldMessageHandler = connection.getMessageHandler();
		connection.setMessageHandler(this );
	}
	
	public Map getNextRequestContextMap(){
		return this.benchmark.getNextRequestContextMap();
	}
	
	public abstract T createRequestPacket();

	public abstract T decodeRecievedPacket(byte[] message);

	public void startBenchmark(){
		postPacketToServer();
	}
	
	protected void afterMessageRecieved(byte[] message){
		
	}
	
	protected void doReceiveMessage(byte[] message) {
		boolean completed = responseIsCompleted(message);
		if (debug) {
			T t = decodeRecievedPacket(message);
			System.out.println("<<--" + t);
		}
		
		afterMessageRecieved(message);
		
		if(completed){
			end = System.nanoTime();
			long current = end - next;
			next = end;
			min = Math.min(min, current);
			max = Math.max(max, current);
			count++;
			afterResponseCompleted();
		}
	}
	
	public void handleMessage(Connection conn){
		
		if(conn instanceof AuthingableConnection){
			if(!((AuthingableConnection)conn).isAuthenticated()){
				connOldMessageHandler.handleMessage(conn);
			}else{
				byte[] message = null;
				while((message = conn.getInQueue().getNonBlocking()) != null){
					doReceiveMessage(message);
				}
			}
		}else{
			connOldMessageHandler.handleMessage(conn);
		}
	}
	
	protected boolean responseIsCompleted(byte[] message){
		return true;
	}
	
	protected void afterResponseCompleted(){
		responseLatcher.countDown();
		if(task.running){
			if(requestLatcher.getCount()>0){
				requestLatcher.countDown();
				postPacketToServer();
			}
		}
	}

	protected void postPacketToServer(){
		T packet = createRequestPacket();
		ByteBuffer buffer = packet.toByteBuffer(connection);
		if (debug) {
			System.out.println("--->>" + packet);
		}
		connection.postMessage(buffer);
	}

	public void init() {
	}
	
	/*public boolean checkIdle(long now) {
		boolean isTimeOut = false;
		if(timeout>0){
			if (connection.isClosed()) {
				isTimeOut = true;
	        }
			long idleMillis = now - connection._lastEvent;
	        if (idleMillis < timeout) {
	        	isTimeOut = false;
	        }else{
	        	isTimeOut = true;
	        }
		}else{
			isTimeOut = false;
		}
		
		if(isTimeOut){
			logger.warn("socket id="+this.getSocketId()+" receive time out="+(now - _lastEvent));			
		}
		return isTimeOut;
	}*/
}