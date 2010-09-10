package com.meidusa.amoeba.mongodb.handler;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;

public class DeleteMessageHandler extends ModifyOperateMessageHandler<DeleteMongodbPacket> {

	public DeleteMessageHandler(MongodbClientConnection clientConn,
			DeleteMongodbPacket t) {
		super(clientConn, t);
	}

}
