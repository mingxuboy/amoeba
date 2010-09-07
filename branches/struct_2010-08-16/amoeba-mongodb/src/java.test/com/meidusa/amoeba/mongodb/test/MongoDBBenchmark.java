package com.meidusa.amoeba.mongodb.test;


import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import com.meidusa.amoeba.benchmark.AbstractBenchmark;
import com.meidusa.amoeba.benchmark.AbstractBenchmarkClientConnection;
import com.meidusa.amoeba.util.InitialisationException;

public class MongoDBBenchmark extends AbstractBenchmark{
	private MongoDBBenchmark(){
		AbstractBenchmark.setBenchmark(this);
	}
	public static void main(String[] args) throws IOException,
			InterruptedException, InitialisationException {
		new MongoDBBenchmark();
		AbstractBenchmark.main(args);
	}

	public AbstractBenchmarkClientConnection<?> newBenchmarkClientConnection(
			SocketChannel channel, long time,CountDownLatch latcher) {
		return new MongodbBenchmarkClientConnection(channel,time,latcher);
	}
	
}
