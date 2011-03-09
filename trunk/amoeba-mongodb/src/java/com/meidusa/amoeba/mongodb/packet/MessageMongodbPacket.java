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
 * <h6><a name="MongoWireProtocol-OPMSG"></a>OP_MSG <a name="MongoWireProtocol-OPMSG"></a></h6>
 * 
 * <p>Deprecated. OP_MSG sends a diagnostic message to the database.&nbsp; The database sends back a fixed resonse.&nbsp; The format is</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct {
 *     MsgHeader header;  <span class="code-comment">// standard message header
 * </span>    cstring   message; <span class="code-comment">// message <span class="code-keyword">for</span> the database
 * </span>}
 * </pre>
 * @author Struct
 * @deprecated
 */
public class MessageMongodbPacket extends AbstractMongodbPacket {

	public String message;
	public MessageMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_MSG;
	}
	
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		message = buffer.readCString();
	}
	
	protected void write2Buffer(MongodbPacketBuffer buffer)
	throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeCString(message);
	}
}
