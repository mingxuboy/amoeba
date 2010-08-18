package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

public class InsertMongodbPacket extends AbstractMongodbPacket {
	
	public int ZERO = 0;
	public String fullCollectionName;
	public List<BSONObject> documents;
	public InsertMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_INSERT;
	}
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		buffer.readInt();//ZERO 
		fullCollectionName = buffer.readCString();
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
		buffer.writeInt(0);
		buffer.writeCString(fullCollectionName);
		if(documents != null){
			for(BSONObject doc: documents){
				buffer.writeBSONObject(doc);
			}
		}
	}
	
}
