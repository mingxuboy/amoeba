package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;

import org.bson.BSONObject;

public class QueryMongodbPacket extends AbstractMongodbPacket {

	public int flags;
	public String fullCollectionName;
	public int numberToSkip;
	public int numberToReturn;
	public BSONObject document;
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		flags = buffer.readInt();
		fullCollectionName = buffer.readCString();
		numberToSkip = buffer.readInt();
		numberToReturn = buffer.readInt();
		document = buffer.readBSONObject();
	}
	
	protected void write2Buffer(MongodbPacketBuffer buffer)
	throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(flags);
		buffer.writeCString(fullCollectionName);
		buffer.writeInt(numberToSkip);
		buffer.writeInt(numberToReturn);
		buffer.writeBSONObject(document);
	}
	
}
