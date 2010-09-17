/*
 * Copyright amoeba.meidusa.com
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mongodb.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.BSONObject;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.CursorEntry;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.SimpleResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.route.MongodbQueryRouter;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

public class QueryMessageHandler extends AbstractSessionHandler<QueryMongodbPacket> {
	private static Logger logger = Logger.getLogger("PACKETLOGGER");
	
	private List<Tuple<CursorEntry,ObjectPool>> cursorList;
	public QueryMessageHandler(MongodbClientConnection clientConn,QueryMongodbPacket packet) {
		super(clientConn,packet);
	}

	@Override
	protected void doClientRequest(MongodbClientConnection conn,
			byte[] message) throws Exception {
		if(requestPacket.fullCollectionName.indexOf("$")>0 && requestPacket.query != null 
				&& requestPacket.query.get("getlasterror") != null){
			byte[] msg = clientConn.getLastErrorMessage();
			ResponseMongodbPacket packet = new ResponseMongodbPacket();
			if(msg == null){
				packet.responseTo = this.requestPacket.requestID;
				packet.numberReturned = 1;
				packet.documents = new ArrayList<BSONObject>(1);
				packet.documents.add(BSON_OK);
				logger.error("cannot getLasterrorMessage with requst="+this.requestPacket);
			}else{
				packet.init(msg, conn);
			}
			if(logger.isDebugEnabled()){
				logger.debug("<<----@ReponsePacket="+packet+", " +clientConn.getSocketId());
			}
			clientConn.postMessage(msg);
			return;
		}
		
		MongodbQueryRouter router = (MongodbQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();

		ObjectPool[] pools = router.doRoute(clientConn, requestPacket);
		if(pools == null || pools.length==0){
			pools = router.getDefaultObjectPool();
		}
		
		if(pools != null && pools.length >1){
			isMulti = true;
			cursorList = new ArrayList<Tuple<CursorEntry,ObjectPool>>();
			this.multiResponsePacket = new ArrayList<ResponseMongodbPacket>();
		}
		
		MongodbServerConnection[] conns = new MongodbServerConnection[pools.length];
		int index =0;
		for(ObjectPool pool: pools){
			MongodbServerConnection serverConn = (MongodbServerConnection)pool.borrowObject();
			handlerMap.put(serverConn, serverConn.getMessageHandler());
			serverConn.setSessionMessageHandler(this);
			conns[index++] = serverConn;
		}
		
		for(MongodbServerConnection serverConn : conns){
			serverConn.postMessage(message);
		}
	}

	@Override
	protected void doServerResponse(MongodbServerConnection conn,
			byte[] message) {
		SimpleResponseMongodbPacket packet = null;
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		int type = MongodbPacketBuffer.getOPMessageType(message);
		
		if(type != MongodbPacketConstant.OP_REPLY){
			logger.error("unkown response packet type="+type+" , request="+this.requestPacket);
		}
		
		if(logger.isDebugEnabled() || isMulti){
			packet = new ResponseMongodbPacket();
		}else{
			packet = new SimpleResponseMongodbPacket();
		}
		packet.init(message, conn);
		if(logger.isDebugEnabled()){
			putDebugInfoToPacket((ResponseMongodbPacket)packet,conn);
		}
		if(isMulti){
			multiResponsePacket.add((ResponseMongodbPacket)packet);
			if(packet.cursorID >0){
				CursorEntry entry = new CursorEntry();
				entry.cursorID = packet.cursorID;
				entry.fullCollectionName = this.requestPacket.fullCollectionName;
				Tuple<CursorEntry,ObjectPool> tuple = new Tuple<CursorEntry,ObjectPool>(entry,serverConn.getObjectPool());
				this.cursorList.add(tuple);
			}
			
			if(endQuery(conn)){
				long cursrID = 0;
				if(cursorList.size()>=1){
					cursrID = this.clientConn.nextCursorID();
					clientConn.putCursor(cursrID, cursorList);
				}
				ResponseMongodbPacket result = mergeResponse();
				result.cursorID = cursrID;
				clientConn.postMessage(result.toByteBuffer(this.clientConn));
			}
		}else{
		
			if(packet.cursorID >0){
				CursorEntry entry = new CursorEntry();
				entry.cursorID = packet.cursorID;
				
				entry.fullCollectionName = this.requestPacket.fullCollectionName;
				
				Tuple<CursorEntry,ObjectPool> tuple = new Tuple<CursorEntry,ObjectPool>(entry,serverConn.getObjectPool());
				clientConn.addCursorItem(packet.cursorID, tuple);
			}
			endQuery(conn);
			clientConn.postMessage(message);
		}
	}

}
