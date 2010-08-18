package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

public class ResponseMongodbPacket extends AbstractMongodbPacket {
	public ResponseMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_REPLY;
	}
	public int responseFlags;
	public long cursorID;
	public int startingFrom;
	public int numberReturned;
	public List<BSONObject> documents;
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		responseFlags = buffer.readInt();
		cursorID = buffer.readLong();
		startingFrom = buffer.readInt();
		numberReturned = buffer.readInt();
		if(buffer.hasRemaining()){
			documents = new ArrayList<BSONObject>();
			do{
				BSONObject obj = buffer.readBSONObject();
				documents.add(obj);
			}while(buffer.hasRemaining());
		}
	}

	@Override
	protected void write2Buffer(MongodbPacketBuffer buffer)
			throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(responseFlags);
		buffer.writeLong(cursorID);
		buffer.writeInt(startingFrom);
		buffer.writeInt(numberReturned);
		if(documents != null){
			for(BSONObject doc: documents){
				buffer.writeBSONObject(doc);
			}
		}
	}

}
