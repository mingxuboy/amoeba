package com.meidusa.amoeba.aladdin.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.mysql.net.MysqlClientConnection;

public class AladdinClientConnection extends MysqlClientConnection {

	public AladdinClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	public void setSchema(String schema) {
		//ignore client mysql schema
		//	this.schema = schema;
	}
}
