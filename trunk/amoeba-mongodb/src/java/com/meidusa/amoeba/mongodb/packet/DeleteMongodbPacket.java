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

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

/**
 * <h6><a name="MongoWireProtocol-OPDELETE"></a>OP_DELETE <a name="MongoWireProtocol-OPDELETE"></a></h6>
 * 
 * <p>The OP_DELETE message is used to remove one or more messages from a collection.  The format of the OP_DELETE message is :</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct {
 *     MsgHeader header;             <span class="code-comment">// standard message header
 * </span>    int32     ZERO;               <span class="code-comment">// 0 - reserved <span class="code-keyword">for</span> <span class="code-keyword">future</span> use
 * </span>    cstring   fullCollectionName; <span class="code-comment">// <span class="code-quote">"dbname.collectionname"</span>
 * 
 * </span>    int32     flags;              <span class="code-comment">// bit vector - see below <span class="code-keyword">for</span> details.
 * </span>    document  selector;           <span class="code-comment">// query object.  See below <span class="code-keyword">for</span> details.
 * </span>}
 * </pre>

 * @author Struct
 *
 */
public class DeleteMongodbPacket extends RequestMongodbPacket {
	
	public int flags;
	public BSONObject selector;
	public DeleteMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_DELETE;
	}
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		flags = buffer.readInt();
		if(buffer.hasRemaining()){
			selector  = buffer.readBSONObject();
		}
	}

	@Override
	protected void write2Buffer(MongodbPacketBuffer buffer)
			throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(flags);
		buffer.writeBSONObject(selector);
	}
	
}
