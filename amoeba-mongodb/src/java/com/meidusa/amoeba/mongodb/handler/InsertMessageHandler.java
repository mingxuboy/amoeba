package com.meidusa.amoeba.mongodb.handler;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;

public class InsertMessageHandler extends ModifyOperateMessageHandler<InsertMongodbPacket> {

	public InsertMessageHandler(MongodbClientConnection clientConn,
			InsertMongodbPacket t) {
		super(clientConn, t);
	}

}
