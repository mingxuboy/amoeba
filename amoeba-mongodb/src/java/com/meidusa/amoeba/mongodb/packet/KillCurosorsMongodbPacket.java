package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

/**
 * <h6><a name="MongoWireProtocol-OPKILLCURSORS"></a>OP_KILL_CURSORS <a name="MongoWireProtocol-OPKILLCURSORS"></a></h6>
 * 
 * <p>The OP_KILL_CURSORS message is used to close an active cursor in the database.  This is necessary to ensure that database resources are reclaimed at the end of the query.  The format of the OP_KILL_CURSORS message is :</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct {
 *     MsgHeader header;            <span class="code-comment">// standard message header
 * </span>    int32     ZERO;              <span class="code-comment">// 0 - reserved <span class="code-keyword">for</span> <span class="code-keyword">future</span> use
 * </span>    int32     numberOfCursorIDs; <span class="code-comment">// number of cursorIDs in message
 * </span>    int64*    cursorIDs;         <span class="code-comment">// sequence of cursorIDs to close
 * </span>}
 * </pre>
 * @author Struct
 *
 */
public class KillCurosorsMongodbPacket extends AbstractMongodbPacket {
	public int ZERO = 0;
	public String fullCollectionName;
	public int numberOfCursorIDs;
	public long[] cursorIDs;
	
	public KillCurosorsMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_KILL_CURSORS;
	}
	
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		buffer.readInt();//ZERO 
		fullCollectionName = buffer.readCString();
		numberOfCursorIDs = buffer.readInt();
		if(numberOfCursorIDs >0){
			cursorIDs = new long[numberOfCursorIDs];
			for(int i=0;i<numberOfCursorIDs;i++){
				cursorIDs[i] = buffer.readLong();
			}
		}
	}
	
	protected void write2Buffer(MongodbPacketBuffer buffer)
	throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(0);//ZERO
		buffer.writeCString(fullCollectionName);
		if(cursorIDs != null && cursorIDs.length >0){
			buffer.writeInt(cursorIDs.length);
			for(int i=0;i<cursorIDs.length;i++){
				buffer.writeLong(cursorIDs[i]);
			}
		}
	}
}
