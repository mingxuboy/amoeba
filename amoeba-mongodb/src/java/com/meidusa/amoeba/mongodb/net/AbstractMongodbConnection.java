package com.meidusa.amoeba.mongodb.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;

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
	
	@Override
	protected PacketInputStream createPacketInputStream() {
		return new MongodbFramedInputStream(true);
	}

	@Override
	protected PacketOutputStream createPacketOutputStream() {
		return new MongodbFramingOutputStream(true);
	}
}
