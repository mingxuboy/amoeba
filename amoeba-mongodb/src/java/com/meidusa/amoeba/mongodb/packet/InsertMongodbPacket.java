package com.meidusa.amoeba.mongodb.packet;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

public class InsertMongodbPacket extends AbstractMongodbPacket {
	
	public int ZERO = 0;
	public String fullCollectionName;
	
	//public Document doucment;
	public InsertMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_INSERT;
	}
	
	
}
