package com.meidusa.amoeba.mongodb.test;


import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.bson.BSONObject;
import org.bson.JSON;

import com.meidusa.amoeba.benchmark.AbstractBenchmark;
import com.meidusa.amoeba.benchmark.AbstractBenchmarkClientConnection;
import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.config.PropertyTransfer;

public class MongoDBBenchmark extends AbstractBenchmark{
	private Map contextMap = new HashMap();
	private Properties properties = new Properties();
	private MongoDBBenchmark(){
		AbstractBenchmark.setBenchmark(this);
		Random random = new Random();
		contextMap.put("random",random);
		contextMap.put("atomicInteger",new AtomicInteger());
		contextMap.put("atomicLong",new AtomicLong());
		try {
			properties.loadFromXML(this.getClass().getResourceAsStream("Ognl.xml"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		ParameterMapping.registerTransfer(BSONObject.class, new PropertyTransfer<BSONObject>(){
			@Override
			public BSONObject transfer(String inputString) {
				return (BSONObject)JSON.parse(ConfigUtil.filterWtihOGNL(inputString, AbstractBenchmark.getInstance().getContextMap()));
			}
		});
		new MongoDBBenchmark();
		AbstractBenchmark.main(args);
	}

	public AbstractBenchmarkClientConnection<?> newBenchmarkClientConnection(
			SocketChannel channel, long time,CountDownLatch latcher) {
		AbstractBenchmarkClientConnection conn = new MongodbBenchmarkClientConnection(channel,time,latcher);
		conn.putAllRequestProperties(properties);
		return conn;
	}

	@Override
	public Map getContextMap() {
		return contextMap;
	}
	
}
