package com.meidusa.amoeba.mongodb.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.handler.CommandMessageHandler;
import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;

public class MongodbClientConnection extends Connection implements MessageHandler{

	public MongodbClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	@Override
	protected PacketInputStream createPacketInputStream() {
		return new MongodbFramedInputStream(true);
	}

	@Override
	protected PacketOutputStream createPakcetOutputStream() {
		return new MongodbFramingOutputStream(true);
	}

	@Override
	public void handleMessage(Connection conn) {
		try {
			Connection serverConn = (Connection)ProxyRuntimeContext.getInstance().getPoolMap().get("server1").borrowObject();
			new CommandMessageHandler(this,serverConn).handleMessage(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
