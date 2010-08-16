package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;

public class QueryMongodbPacket extends AbstractMongodbPacket {

	public int flags;
	public String fullCollectionName;
	public int numberToSkip;
	public int numberToReturn;
	
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		
	}
	
	protected void write2Buffer(MongodbPacketBuffer buffer)
	throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		
	}
	
}
