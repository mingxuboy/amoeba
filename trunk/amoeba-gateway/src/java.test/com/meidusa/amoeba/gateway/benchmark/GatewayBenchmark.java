package com.meidusa.amoeba.gateway.benchmark;


import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.benchmark.AbstractBenchmark;
import com.meidusa.amoeba.benchmark.AbstractBenchmarkClientConnection;
import com.meidusa.amoeba.util.CmdLineParser;

public class GatewayBenchmark extends AbstractBenchmark{
	private static Logger logger = Logger.getLogger(GatewayBenchmark.class);
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
		
		AbstractBenchmark.setBenchmark(new GatewayBenchmark());
		AbstractBenchmark.main(args);
	}

	public AbstractBenchmarkClientConnection<?> newBenchmarkClientConnection(
			SocketChannel channel, long time,CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task) {
		AbstractBenchmarkClientConnection conn = new GatewayBenchmarkClientConnection(channel,time,requestLatcher,responseLatcher,task);
		return conn;
	}
	
}
