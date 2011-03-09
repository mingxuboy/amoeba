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
 * <h6><a name="MongoWireProtocol-OPQUERY"></a>OP_QUERY <a name="MongoWireProtocol-OPQUERY"></a></h6>
 * 
 * <p>The OP_QUERY message is used to query the database for documents in a collection.  The format of the OP_QUERY message is :</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct OP_QUERY {
 *     MsgHeader header;                <span class="code-comment">// standard message header
 * </span>    int32     flags;                  <span class="code-comment">// bit vector of query options.  See below <span class="code-keyword">for</span> details.
 * 
 * </span>    cstring   fullCollectionName;    <span class="code-comment">// <span class="code-quote">"dbname.collectionname"</span>
 * </span>    int32     numberToSkip;          <span class="code-comment">// number of documents to skip
 * </span>    int32     numberToReturn;        <span class="code-comment">// number of documents to <span class="code-keyword">return</span>
 * </span>                                     <span class="code-comment">//  in the first OP_REPLY batch
 * </span>    document  query;                 <span class="code-comment">// query object.  See below <span class="code-keyword">for</span> details.
 * 
 * </span>  [ document  returnFieldSelector; ] <span class="code-comment">// Optional. Selector indicating the fields
 * </span>                                     <span class="code-comment">//  to <span class="code-keyword">return</span>.  See below <span class="code-keyword">for</span> details.
 * </span>}
 * </pre>
 * 
 * @author Struct
 *
 */
public class QueryMongodbPacket extends RequestMongodbPacket {

	public int numberToSkip;
	public int numberToReturn;
	public BSONObject query;
	public BSONObject returnFieldSelector;
	public QueryMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_QUERY;
	}
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		numberToSkip = buffer.readInt();
		numberToReturn = buffer.readInt();
		query = buffer.readBSONObject();
		
		if(buffer.hasRemaining()){
			returnFieldSelector  = buffer.readBSONObject();
		}
	}
	
	protected void write2Buffer(MongodbPacketBuffer buffer)
	throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(numberToSkip);
		buffer.writeInt(numberToReturn);
		buffer.writeBSONObject(query);//can not be null
		if(returnFieldSelector != null){
			buffer.writeBSONObject(returnFieldSelector);
		}
	}
	
	public boolean isRead() {
		return true;
	}
	
}
