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
import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

/**
 * <h6><a name="MongoWireProtocol-OPREPLY"></a>OP_REPLY <a name="MongoWireProtocol-OPREPLY"></a></h6>
 * 
 * <p>The OP_REPLY message is sent by the database in response to an <a href="#MongoWireProtocol-OPQUERY">CONTRIB:OP_QUERY</a> or <a href="#MongoWireProtocol-OPGETMORE">CONTRIB:OP_GET_MORE</a> <br/>
 * 
 * message.  The format of an OP_REPLY message is:</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct {
 *     MsgHeader header;         <span class="code-comment">// standard message header
 * </span>    int32     responseFlags;  <span class="code-comment">// bit vector - see details below
 * </span>    int64     cursorID;       <span class="code-comment">// cursor id <span class="code-keyword">if</span> client needs to <span class="code-keyword">do</span> get more's
 * 
 * </span>    int32     startingFrom;   <span class="code-comment">// where in the cursor <span class="code-keyword">this</span> reply is starting
 * </span>    int32     numberReturned; <span class="code-comment">// number of documents in the reply
 * </span>    document* documents;      <span class="code-comment">// documents
 * </span>}
 * </pre>
 * 
 * @author Struct
 *
 */
public class ResponseMongodbPacket extends SimpleResponseMongodbPacket {
	public int startingFrom;
	public int numberReturned;
	public List<BSONObject> documents;
	
	public ResponseMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_REPLY;
	}
	
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
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
		buffer.writeInt(startingFrom);
		buffer.writeInt(numberReturned);
		if(documents != null){
			for(BSONObject doc: documents){
				buffer.writeBSONObject(doc);
			}
		}
	}

}
