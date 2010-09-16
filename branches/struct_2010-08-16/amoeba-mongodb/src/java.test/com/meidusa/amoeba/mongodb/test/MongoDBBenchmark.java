package com.meidusa.amoeba.mongodb.test;


import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import com.meidusa.amoeba.benchmark.AbstractBenchmark;
import com.meidusa.amoeba.benchmark.AbstractBenchmarkClientConnection;

public class MongoDBBenchmark extends AbstractBenchmark{
	
	public static void main(String[] args) throws Exception {
		
		AbstractBenchmark.setBenchmark(new MongoDBBenchmark());
		AbstractBenchmark.main(args);
	}

	public AbstractBenchmarkClientConnection<?> newBenchmarkClientConnection(
			SocketChannel channel, long time,CountDownLatch latcher) {
		AbstractBenchmarkClientConnection conn = new MongodbBenchmarkClientConnection(channel,time,latcher);
		return conn;
	}
	
}
