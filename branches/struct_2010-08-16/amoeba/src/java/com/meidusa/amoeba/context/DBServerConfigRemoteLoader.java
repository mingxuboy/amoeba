package com.meidusa.amoeba.context;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

import com.meidusa.amoeba.config.DBServerConfig;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionFactory;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;

public class DBServerConfigRemoteLoader implements Initialisable{
	private ConnectionManager manager;
	private ConnectionFactory connectionFactory;
	private String ipAddress;
	private int port;
	private Connection connection;
	public ConnectionManager getManager() {
		return manager;
	}

	public void setManager(ConnectionManager manager) {
		this.manager = manager;
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Map<String,DBServerConfig> loadConfig(){
		return null;
	}
	
	@Override
	public void init() throws InitialisationException {
		try {
			SocketChannel channel = SocketChannel.open(new InetSocketAddress(ipAddress,port));
			connection = connectionFactory.createConnection(channel, System.currentTimeMillis());
			this.manager.postRegisterNetEventHandler(connection, SelectionKey.OP_READ);
			
		} catch (IOException e) {
			throw new InitialisationException("cannot create connect to host="+ipAddress+":"+port,e);
		}
	}
	
}