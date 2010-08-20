package com.meidusa.amoeba.mongodb.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.Connection;

public abstract class AbstractMongodbConnection extends Connection {
	public AbstractMongodbConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	public void postMessage(byte[] msg) {
		postMessage(ByteBuffer.wrap(msg));
	}

}
