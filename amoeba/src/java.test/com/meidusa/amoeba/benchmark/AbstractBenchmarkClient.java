package com.meidusa.amoeba.benchmark;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.benchmark.AbstractBenchmark.TaskRunnable;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.Packet;

public abstract class AbstractBenchmarkClient<T extends Packet> {
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
	}
	
	public Map getNextRequestContextMap(){
		return this.benchmark.getNextRequestContextMap();
	}
	
	public abstract T createRequestPacket();

	public abstract T createPacketWithBytes(byte[] message);

	public abstract void startBenchmark();
	
	protected void doReceiveMessage(byte[] message) {
		
		end = System.nanoTime();
		long current = end - next;
		min = Math.min(min, current);
		max = Math.max(max, current);
		count++;

		if (debug) {
			T t = createPacketWithBytes(message);
			System.out.println("<<--" + t);
		}
		
		if(responseIsCompleted(message)){
			doAfterResponse();
		}
	}
	
	protected boolean responseIsCompleted(byte[] message){
		return true;
	}
	
	protected void doAfterResponse(){
		responseLatcher.countDown();
		postPacketToServer();
	}

	protected void postPacketToServer(){
		if(task.running){
			if(requestLatcher.getCount()>0){
				requestLatcher.countDown();
				connection.postMessage(createRequestPacket().toByteBuffer(connection));
			}
		}
	}

	public void init() {
		// TODO Auto-generated method stub
		
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
	
	/*public void postMessage(ByteBuffer msg) {
		next = System.nanoTime();
		if (debug) {
			T t = createPacketWithBytes(msg.array());
			System.out.println("--->>" + t);
		}
		super.postMessage(msg);

	}*/
}