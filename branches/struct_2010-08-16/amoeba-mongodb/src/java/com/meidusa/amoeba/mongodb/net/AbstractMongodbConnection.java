package com.meidusa.amoeba.mongodb.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.SessionMessageHandler;

public abstract class AbstractMongodbConnection extends Connection {
	
	
	public AbstractMongodbConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

    protected abstract void doReceiveMessage(byte[] message);
    
    protected void messageProcess() {
		//_handler.handleMessage(this);
    }
    
	public void postMessage(byte[] msg) {
		postMessage(ByteBuffer.wrap(msg));
	}
	
}
