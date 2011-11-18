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
import java.lang.reflect.Array;
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
public class InsertMongodbPacket extends RequestMongodbPacket {
	
	public BSONObject[] documents;
	public InsertMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_INSERT;
	}
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		if(buffer.hasRemaining()){
			List<BSONObject> documents = new ArrayList<BSONObject>();
			do{
				BSONObject obj = buffer.readBSONObject();
				documents.add(obj);
			}while(buffer.hasRemaining());
			if(documents.size()>0){
				this.documents = documents.toArray((BSONObject[])Array.newInstance(BSONObject.class, documents.size()));
			}
		}
	}

	@Override
	protected void write2Buffer(MongodbPacketBuffer buffer)
			throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		if(documents != null){
			for(BSONObject doc: documents){
				buffer.writeBSONObject(doc);
			}
		}
	}
	
}
