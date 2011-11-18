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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.CursorEntry;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.SimpleResponseMongodbPacket;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

public class GetMoreMessageHandler extends AbstractSessionHandler<GetMoreMongodbPacket> {
	private long cursorID;
	private Map<MongodbServerConnection,CursorEntry> cursorMap = null;
	public GetMoreMessageHandler(MongodbClientConnection clientConn,
			GetMoreMongodbPacket t) {
		super(clientConn, t);
	}

	@Override
	protected void doClientRequest(MongodbClientConnection conn, byte[] message)
			throws Exception {
		List<Tuple<CursorEntry,ObjectPool>> tuples = (List<Tuple<CursorEntry,ObjectPool>>)clientConn.getCursor(cursorID);
		
		if(tuples == null || tuples.size() ==0){
			//TODO need to fill packet field, return cursor not found message
			ResponseMongodbPacket response = new ResponseMongodbPacket();
			clientConn.postMessage(response.toByteBuffer(clientConn));
			return;
		}
		
		this.cursorID = this.requestPacket.cursorID;
		
		if(tuples != null && tuples.size() > 1){
			isMulti = true;
			this.multiResponsePacket = new ArrayList<ResponseMongodbPacket>();
			cursorMap = new HashMap<MongodbServerConnection,CursorEntry>();
		}
		
		int index =0;
		MongodbServerConnection[] conns = new MongodbServerConnection[tuples.size()];
		for(Tuple<CursorEntry,ObjectPool> tuple: tuples){
			MongodbServerConnection serverConn = (MongodbServerConnection)tuple.right.borrowObject();
			handlerMap.put(serverConn, serverConn.getMessageHandler());
			serverConn.setSessionMessageHandler(this);
			conns[index++] = serverConn;
			cursorMap.put(serverConn, tuple.left);
		}
		
		index = 0;
		for(Tuple<CursorEntry,ObjectPool> tuple: tuples){
			this.requestPacket.cursorID = tuple.left.cursorID;
			conns[index].postMessage(requestPacket.toByteBuffer(this.clientConn));
			index++;
		}
	}

	@Override
	protected void doServerResponse(MongodbServerConnection conn, byte[] message) {
		SimpleResponseMongodbPacket packet = null;
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		int type = MongodbPacketBuffer.getOPMessageType(message);
		
		if(type != MongodbPacketConstant.OP_REPLY){
			PACKET_LOGGER.error("unkown response packet type="+type+" , request="+this.requestPacket);
		}
		
		if(PACKET_LOGGER.isDebugEnabled() || isMulti){
			packet = new ResponseMongodbPacket();
		}else{
			packet = new SimpleResponseMongodbPacket();
		}
		packet.init(message, conn);
		
		if(PACKET_LOGGER.isDebugEnabled()){
			PACKET_LOGGER.debug("<<---["+this.requestPacket.requestID+"]--pakcet="+packet+"," +conn.getSocketId());
		}
		
		if(ROUTER_TRACE.isDebugEnabled()){
			putDebugInfoToResponsePacket((ResponseMongodbPacket)packet,conn);
		}
		if(packet.cursorID <= 0){
			this.clientConn.removeCursorItem(this.cursorID,cursorMap.get(serverConn));
		}
		
		if(isMulti){
			multiResponsePacket.add((ResponseMongodbPacket)packet);

			if(endQuery(conn)){
				ResponseMongodbPacket result = mergeResponse(this.requestPacket.numberToReturn == -1);
				result.cursorID = cursorID;
				clientConn.postMessage(result.toByteBuffer(this.clientConn));
			}
		}else{
			endQuery(conn);
			clientConn.postMessage(message);
		}
	}

}
