package com.meidusa.amoeba.benchmark;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.Packet;

public abstract class AbstractBenchmarkClientConnection<T extends Packet>
		extends Connection {
	private static boolean debug = Boolean.getBoolean("debug");
	private Properties properties;
	long min = System.nanoTime();
	long start = 0;
	long max = 0;
	long end = min;
	long next = min;
	long count = 0;
	protected CountDownLatch latcher;
	private Map contextMap; 
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
			long createStamp, CountDownLatch latcher) {
		super(channel, createStamp);
		start = System.nanoTime();
		this.latcher = latcher;
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
		latcher.countDown();
		end = System.nanoTime();
		long current = end - next;
		min = Math.min(min, current);
		max = Math.max(max, current);
		count++;

		if (debug) {
			T t = createPacketWithBytes(message);
			System.out.println("<<--" + t);
		}

		if (latcher.getCount() <= 0) {
			return;
		}
		postMessage(createRequestPacket().toByteBuffer(this));
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