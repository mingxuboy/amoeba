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
 * <h6><a name="MongoWireProtocol-OPUPDATE"></a>OP_UPDATE <a name="MongoWireProtocol-OPUPDATE"></a></h6>
 * 
 * <p>The OP_UPDATE message is used to update a document in a collection.  The format of a OP_UPDATE message is</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct OP_UPDATE {
 *     MsgHeader header;             <span class="code-comment">// standard message header
 * </span>    int32     ZERO;               <span class="code-comment">// 0 - reserved <span class="code-keyword">for</span> <span class="code-keyword">future</span> use
 * 
 * </span>    cstring   fullCollectionName; <span class="code-comment">// <span class="code-quote">"dbname.collectionname"</span>
 * </span>    int32     updateFlags;              <span class="code-comment">// bit vector. see below
 * </span>    document  selector;           <span class="code-comment">// the query to select the document
 * </span>    document  update;             <span class="code-comment">// specification of the update to perform
 * </span>}
 * </pre>
 * 
 * @author Struct
 *
 */
public class UpdateMongodbPacket extends RequestMongodbPacket {
	
	public int updateFlags;
	public BSONObject selector;
	public BSONObject update;
	public UpdateMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_UPDATE;
	}
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		updateFlags = buffer.readInt();
		if(buffer.hasRemaining()){
			selector = buffer.readBSONObject();
		}
		if(buffer.hasRemaining()){
			update = buffer.readBSONObject();
		}
	}

	@Override
	protected void write2Buffer(MongodbPacketBuffer buffer)
			throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(updateFlags);
		if(selector != null){
			buffer.writeBSONObject(selector);
		}
		
		if(update != null){
			buffer.writeBSONObject(update);
		}
	}
	
}
