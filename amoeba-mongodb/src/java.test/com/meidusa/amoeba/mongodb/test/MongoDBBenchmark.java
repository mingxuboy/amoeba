package com.meidusa.amoeba.mongodb.test;


import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.BSONObject;
import org.bson.JSON;

import com.meidusa.amoeba.benchmark.AbstractBenchmark;
import com.meidusa.amoeba.benchmark.AbstractBenchmarkClient;
import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.config.PropertyTransfer;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnectionFactory;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionFactory;
import com.meidusa.amoeba.util.CmdLineParser;

public class MongoDBBenchmark extends AbstractBenchmark{
	private static Logger logger = Logger.getLogger(MongoDBBenchmark.class);
	public static void main(String[] args) throws Exception {
        try {
        	requestOption.setRequired(true);
            parser.parse(args);
            Boolean value = (Boolean)parser.getOptionValue(helpOption,false);
        	if(value != null && value.booleanValue()){
        		parser.printUsage();
        		System.exit(2);
        	}
        	parser.checkRequired();
        }catch ( CmdLineParser.OptionException e ) {
        	System.err.println(e.getMessage());
        	parser.printUsage();
        	System.exit(2);
        }
		ParameterMapping.registerTransfer(BSONObject.class, new PropertyTransfer<BSONObject>(){
			@Override
			public BSONObject transfer(String inputString) {
				String json = ConfigUtil.filterWtihOGNL(inputString, AbstractBenchmark.getInstance().getNextRequestContextMap());
				return (BSONObject)JSON.parse(json);
			}
		});
		
		ParameterMapping.registerTransfer(BSONObject[].class, new PropertyTransfer<BSONObject[]>(){
			@Override
			public BSONObject[] transfer(String inputString) {
				String[] items = StringUtils.splitByWholeSeparator(inputString,"//--");
				BSONObject[] list = new BSONObject[items.length];
				for(int i=0;i<items.length;i++){
					String json = ConfigUtil.filterWtihOGNL(items[i].trim(), AbstractBenchmark.getInstance().getNextRequestContextMap());
					list[i] = (BSONObject)JSON.parse(json);
				}
				return list;
			}
		});	
		
		AbstractBenchmark.setBenchmark(new MongoDBBenchmark());
		AbstractBenchmark.main(args);
	}

	private ConnectionFactory factory = new MongodbClientConnectionFactory();
	@Override
	public ConnectionFactory getConnectionFactory() {
		return factory;
	}

	@Override
	public AbstractBenchmarkClient<?> newBenchmarkClient(Connection conn,
			CountDownLatch requestLatcher, CountDownLatch responseLatcher,
			TaskRunnable task) {
		return new MongodbBenchmarkClient(conn,requestLatcher,responseLatcher,task);
	}
	
}
