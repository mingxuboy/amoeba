package com.meidusa.amoeba.benchmark;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.benchmark.AbstractBenchmark.TaskRunnable;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.Packet;

public abstract class AbstractBenchmarkClientConnection<T extends Packet>
		extends Connection {
	private static Logger       logger        = Logger.getLogger(AbstractBenchmarkClientConnection.class);
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
	private Map contextMap; 
	
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

	public void putAllRequestProperties(Properties source){
		if(properties == null){
			properties = new Properties();
		}
		properties.putAll(source);
	}
	
	public  Properties getRequestProperties(){
		return properties;
	}
	
	public AbstractBenchmarkClientConnection(SocketChannel channel,
			long createStamp, CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task) {
		super(channel, createStamp);
		start = System.nanoTime();
		this.requestLatcher = requestLatcher;
		this.responseLatcher = responseLatcher;
		this.task = task;
	}

	
	
	public void setContextMap(Map contextMap) {
		this.contextMap = contextMap;
	}

	public Map getContextMap(){
		return this.contextMap;
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
		responseLatcher.countDown();
		postPacketToServer();
	}

	protected void postPacketToServer(){
		if(task.running){
			if(requestLatcher.getCount()>0){
				requestLatcher.countDown();
				postMessage(createRequestPacket().toByteBuffer(this));
			}
		}
	}
	
	public boolean checkIdle(long now) {
		boolean isTimeOut = false;
		if(timeout>0){
			if (isClosed()) {
				isTimeOut = true;
	        }
			long idleMillis = now - _lastEvent;
	        if (idleMillis < timeout) {
	        	isTimeOut = false;
	        }else{
	        	isTimeOut = true;
	        }
		}else{
			isTimeOut = false;
		}
		
		if(isTimeOut){
			if(logger.isInfoEnabled()){
				logger.info("socket id="+this.getSocketId()+" receive time out="+(now - _lastEvent));			
			}
		}
		return isTimeOut;
	}
	
	public void postMessage(ByteBuffer msg) {
		next = System.nanoTime();
		if (debug) {
			T t = createPacketWithBytes(msg.array());
			System.out.println("--->>" + t);
		}
		super.postMessage(msg);

	}
}