package com.meidusa.amoeba.mysql.benchmark;


import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.benchmark.AbstractBenchmark;
import com.meidusa.amoeba.benchmark.AbstractBenchmarkClient;
import com.meidusa.amoeba.mysql.net.MysqlServerConnectionFactory;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionFactory;
import com.meidusa.amoeba.util.CmdLineParser;

public class MysqlBenchmark extends AbstractBenchmark{
	private static Logger logger = Logger.getLogger(MysqlBenchmark.class);
	public static void main(String[] args) throws Exception {
        try {
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
		
		AbstractBenchmark.setBenchmark(new MysqlBenchmark());
		AbstractBenchmark.main(args);
	}

	public AbstractBenchmarkClient<?> newBenchmarkClient(
			Connection connection,CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task) {
		AbstractBenchmarkClient client = new MysqlBenchmarkClient(connection,requestLatcher,responseLatcher,task);
		return client;
	}
	
	private ConnectionFactory factory = new MysqlServerConnectionFactory();
	@Override
	public ConnectionFactory getConnectionFactory() {
		return factory;
	}
	
}
