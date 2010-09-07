package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

/**
 * <p>The database will respond to an OP_QUERY message with an <a href="#MongoWireProtocol-OPREPLY">CONTRIB:OP_REPLY</a> message.</p>
 * 
 * 
 * <h6><a name="MongoWireProtocol-OPGETMORE"></a>OP_GETMORE <a name="MongoWireProtocol-OPGETMORE"></a></h6>
 * 
 * <p>The OP_GETMORE message is used to query the database for documents in a collection.  The format of the OP_GETMORE message is :</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct {
 *     MsgHeader header;             <span class="code-comment">// standard message header
 * </span>    int32     ZERO;               <span class="code-comment">// 0 - reserved <span class="code-keyword">for</span> <span class="code-keyword">future</span> use
 * </span>    cstring   fullCollectionName; <span class="code-comment">// <span class="code-quote">"dbname.collectionname"</span>
 * 
 * </span>    int32     numberToReturn;     <span class="code-comment">// number of documents to <span class="code-keyword">return</span>
 * </span>    int64     cursorID;           <span class="code-comment">// cursorID from the OP_REPLY
 * </span>}
 * </pre>
 * @author Struct
 *
 */
public class GetMoreMongodbPacket extends RequestMongodbPacket {
	public int numberToReturn;
	public long cursorID;
	
	public GetMoreMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_GET_MORE;
	}
	
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		numberToReturn = buffer.readInt();
		cursorID = buffer.readLong();
	}
	
	protected void write2Buffer(MongodbPacketBuffer buffer)
	throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(numberToReturn);
		buffer.writeLong(cursorID);
	}
	
	public boolean isRead() {
		return true;
	}
}
