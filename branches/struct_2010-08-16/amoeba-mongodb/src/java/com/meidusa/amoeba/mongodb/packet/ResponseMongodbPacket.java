package com.meidusa.amoeba.mongodb.packet;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

public class ResponseMongodbPacket extends AbstractMongodbPacket {
	public ResponseMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_REPLY;
	}
	public int responseFlags;
	public long cursorID;
	public int startingFrom;
	public int numberReturned;
}
