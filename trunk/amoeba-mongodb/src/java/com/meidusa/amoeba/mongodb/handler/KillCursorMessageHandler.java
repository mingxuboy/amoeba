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
package com.meidusa.amoeba.mongodb.handler;

import java.util.List;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.CursorEntry;
import com.meidusa.amoeba.mongodb.packet.KillCursorsMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

public class KillCursorMessageHandler extends AbstractSessionHandler<KillCursorsMongodbPacket> {

	public KillCursorMessageHandler(MongodbClientConnection clientConn,
			KillCursorsMongodbPacket t) {
		super(clientConn, t);
	}

	@Override
	protected void doClientRequest(MongodbClientConnection conn, byte[] message)
			throws Exception {
		for(long cursorID:this.requestPacket.cursorIDs){
			List<Tuple<CursorEntry,ObjectPool>> tupes = (List<Tuple<CursorEntry,ObjectPool>>)clientConn.removeCursor(cursorID);
			
			//start close cursor request
			if(tupes != null && tupes.size() >0){
				new CursorCloseMessageHandler(clientConn.getSocketId(),tupes);
			}
		}
		ResponseMongodbPacket response = new ResponseMongodbPacket();
		response.responseTo = this.requestPacket.requestID;
		clientConn.postMessage(response.toByteBuffer(clientConn));
	}

	@Override
	protected void doServerResponse(MongodbServerConnection conn, byte[] message) {
		
	}

}
