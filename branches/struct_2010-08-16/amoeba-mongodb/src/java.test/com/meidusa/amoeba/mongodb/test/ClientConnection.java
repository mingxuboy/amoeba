package com.meidusa.amoeba.mongodb.test;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.mongodb.net.AbstractMongodbConnection;


/**
 * 
 * @author Struct
 *
 */
public class ClientConnection extends AbstractMongodbConnection {
	private static Logger	logger        = Logger.getLogger(ClientConnection.class);

	public ClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	public boolean needPing(long now) {
		return false;
	}

	public boolean checkIdle(long now) {
		return false;
	}

	@Override
	protected void doReceiveMessage(byte[] message) {
		
	}
}
