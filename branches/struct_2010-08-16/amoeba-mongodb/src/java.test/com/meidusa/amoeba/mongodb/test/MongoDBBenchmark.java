package com.meidusa.amoeba.mongodb.test;


import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import org.bson.BSONObject;
import org.bson.JSON;

import com.meidusa.amoeba.benchmark.AbstractBenchmark;
import com.meidusa.amoeba.benchmark.AbstractBenchmarkClientConnection;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.config.PropertyTransfer;

public class MongoDBBenchmark extends AbstractBenchmark{
	private MongoDBBenchmark(){
		AbstractBenchmark.setBenchmark(this);
	}
	public static void main(String[] args) throws Exception {
		ParameterMapping.registerTransfer(BSONObject.class, new PropertyTransfer<BSONObject>(){

			@Override
			public BSONObject transfer(String inputString) {
				return (BSONObject)JSON.parse(inputString);
			}
		});
		new MongoDBBenchmark();
		AbstractBenchmark.main(args);
	}

	public AbstractBenchmarkClientConnection<?> newBenchmarkClientConnection(
			SocketChannel channel, long time,CountDownLatch latcher) {
		return new MongodbBenchmarkClientConnection(channel,time,latcher);
	}
	
}
