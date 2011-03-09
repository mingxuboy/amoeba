/*
 * Copyright amoeba.meidusa.com
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
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
public class KillCursorsMongodbPacket extends RequestMongodbPacket {
	public int numberOfCursorIDs;
	public long[] cursorIDs;
	
	public KillCursorsMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_KILL_CURSORS;
	}
	
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
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
		if(cursorIDs != null && cursorIDs.length >0){
			buffer.writeInt(cursorIDs.length);
			for(int i=0;i<cursorIDs.length;i++){
				buffer.writeLong(cursorIDs[i]);
			}
		}
	}
	
	public boolean isRead() {
		return true;
	}

}
