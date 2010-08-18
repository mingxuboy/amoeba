package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

/**
 * <h6><a name="MongoWireProtocol-OPINSERT"></a>OP_INSERT <a name="MongoWireProtocol-OPINSERT"></a></h6>
 * 
 * <p>The OP_INSERT message is used to insert one or more documents into a collection.  The format of the OP_INSERT message is</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct {
 *     MsgHeader header;             <span class="code-comment">// standard message header
 * </span>    int32     ZERO;               <span class="code-comment">// 0 - reserved <span class="code-keyword">for</span> <span class="code-keyword">future</span> use
 * 
 * </span>    cstring   fullCollectionName; <span class="code-comment">// <span class="code-quote">"dbname.collectionname"</span>
 * </span>    document* documents;          <span class="code-comment">// one or more documents to insert into the collection
 * </span>}
 * </pre>
 * @author Struct
 *
 */
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
