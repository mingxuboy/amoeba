package com.meidusa.amoeba.mongodb.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionFactory;

public class MongodbClientConnectionFactory implements ConnectionFactory{

	@Override
	public Connection createConnection(SocketChannel channel, long createStamp)
			throws IOException {
		MongodbClientConnection conn =  new MongodbClientConnection(channel,createStamp);
		conn.setMessageHandler(conn);
		return conn;
	}

}
