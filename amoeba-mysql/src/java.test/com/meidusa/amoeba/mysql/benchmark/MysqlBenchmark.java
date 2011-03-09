package com.meidusa.amoeba.mysql.benchmark;


import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.benchmark.AbstractBenchmark;
import com.meidusa.amoeba.benchmark.AbstractBenchmarkClient;
import com.meidusa.amoeba.mysql.net.MysqlServerConnection;
import com.meidusa.amoeba.mysql.net.MysqlServerConnectionFactory;
import com.meidusa.amoeba.net.AuthingableConnection;
import com.meidusa.amoeba.net.BackendConnectionFactory;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionFactory;
import com.meidusa.amoeba.util.CmdLineParser;
import com.meidusa.amoeba.util.CmdLineParser.StringOption;

public class MysqlBenchmark extends AbstractBenchmark{
	private static Logger logger = Logger.getLogger(MysqlBenchmark.class);
	protected static CmdLineParser.Option userOption = parser.addOption(new StringOption('u', "user",true,true,"root","mysql user name"));
	protected static CmdLineParser.Option passwordOption = parser.addOption(new StringOption('p', "password",true,false,null,"mysql password"));
	protected static CmdLineParser.Option sqlOption = parser.addOption(new StringOption('s', "sql",true,false,null,"query sql"));
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
			AuthingableConnection conn = (AuthingableConnection) connection;
			if(conn.isAuthenticatedSeted() && !conn.isAuthenticated() && this.getBenchmarkClientList().size() ==0 ){
				System.out.println("start benchmark error,could not cnnect to mysql");
				System.exit(-1);
			}
		return new MysqlBenchmarkClient(connection,requestLatcher,responseLatcher,task);
	}
	
	private BackendConnectionFactory factory = new MysqlServerConnectionFactory(){
		{
			String user = (String)parser.getOptionValue(userOption);
			this.setUser(user);
			String password = (String)parser.getOptionValue(passwordOption);
			this.setPassword(password);
		}
		protected Connection newConnectionInstance(SocketChannel channel,
				long createStamp) {
			MysqlServerConnection conn = new MysqlServerConnection(channel,createStamp);
			
			return conn;
		}
	};
	@Override
	public ConnectionFactory getConnectionFactory() {
		return factory;
	}
	
}
