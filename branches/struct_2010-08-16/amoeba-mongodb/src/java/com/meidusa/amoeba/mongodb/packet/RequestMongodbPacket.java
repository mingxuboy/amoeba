package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;

public abstract class RequestMongodbPacket extends AbstractMongodbPacket {
	public int requestFlags = 0;
	public String fullCollectionName;

	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		requestFlags = buffer.readInt();
		fullCollectionName = buffer.readCString();
	}

	protected void write2Buffer(MongodbPacketBuffer buffer)
			throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(requestFlags);
		buffer.writeCString(fullCollectionName);
	}
}
