package com.meidusa.amoeba.mongodb.packet;

import java.io.IOException;
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
		responseFlags = buffer.readInt();
		cursorID = buffer.readLong();
		startingFrom = buffer.readInt();
		numberReturned = buffer.readInt();
		if(buffer.hasRemaining()){
			documents = new ArrayList<BSONObject>();
		}
		/*while(buffer.hasRemaining()){
			documents.add(buffer.readBSONObject());
		}*/
	}

	@Override
	protected void write2Buffer(MongodbPacketBuffer buffer)
			throws UnsupportedEncodingException {
		buffer.writeInt(numberReturned);
		buffer.writeLong(numberReturned);
		buffer.writeInt(startingFrom);
		buffer.writeInt(numberReturned);
		if(documents != null){
			for(BSONObject doc: documents){
				try {
					buffer.writeBSONObject(doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
