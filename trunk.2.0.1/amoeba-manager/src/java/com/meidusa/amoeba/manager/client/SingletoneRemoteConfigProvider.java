package com.meidusa.amoeba.manager.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;

import com.meidusa.amoeba.heartbeat.HeartbeatDelayed;
import com.meidusa.amoeba.heartbeat.HeartbeatManager;
import com.meidusa.amoeba.heartbeat.Status;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionFactory;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.service.Service;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;

public class SingletoneRemoteConfigProvider implements Initialisable ,Service{
	private ConnectionManager manager;
	private ConnectionFactory connectionFactory;
	private String ipAddress;
	private int port;
	private Connection connection;
	private HeartbeatDelayed heartbeatDelayed = new HeartbeatDelayed(2, TimeUnit.SECONDS){

		@Override
		public Status doCheck() {
			
			return null;
		}

		@Override
		public String getName() {
			return "Amoeba Managed Server Heartbeat";
		}
		
	};
	
	@Override
	public void init() throws InitialisationException {
		try {
			SocketChannel channel = SocketChannel.open(new InetSocketAddress(ipAddress,port));
			connection = connectionFactory.createConnection(channel, System.currentTimeMillis());
			connection.setMessageHandler(new RemoteConfigMessageHandler());
			this.manager.postRegisterNetEventHandler(connection, SelectionKey.OP_READ);
			HeartbeatManager.addHeartbeat(heartbeatDelayed);
		} catch (IOException e) {
			throw new InitialisationException("cannot create connect to host="+ipAddress+":"+port,e);
		}
	}

	@Override
	public void shutdown() {
		if(heartbeatDelayed != null){
			HeartbeatManager.removeHeartbeat(heartbeatDelayed);
		}
		connection.postClose(null);
	}

	@Override
	public void start() {
		
	}

	@Override
	public int getShutdownPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void appendReport(StringBuilder buffer, long now, long sinceLast,
			boolean reset, Level level) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority() {
		return 0;
	}
}
