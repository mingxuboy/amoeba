package com.meidusa.amoeba.mongodb.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.PoolableConnectionFactory;

public class MongodbServerConnectionFactory extends PoolableConnectionFactory{

	@Override
	protected Connection newConnectionInstance(SocketChannel channel,
			long createStamp) {
		return new MongodbServerConnection(channel,createStamp);
	}

}
