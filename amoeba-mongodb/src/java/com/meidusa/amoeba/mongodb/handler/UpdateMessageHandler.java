package com.meidusa.amoeba.mongodb.handler;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;

public class UpdateMessageHandler extends ModifyOperateMessageHandler<UpdateMongodbPacket> {

	public UpdateMessageHandler(MongodbClientConnection clientConn,
			UpdateMongodbPacket t) {
		super(clientConn, t);
	}

}
