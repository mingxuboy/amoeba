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
import java.util.List;

import org.bson.BSONObject;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.handler.merge.FunctionMerge;
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
	
	private List<Tuple<CursorEntry,ObjectPool>> cursorList;
	public QueryMessageHandler(MongodbClientConnection clientConn,QueryMongodbPacket packet) {
		super(clientConn,packet);
	}

	@Override
	protected void doClientRequest(MongodbClientConnection conn,
			byte[] message) throws Exception {
		//request last error
		if(requestPacket.fullCollectionName.indexOf(".$cmd")>0){
			if(requestPacket.query != null){
				if(requestPacket.query.get("getlasterror") != null){
					byte[] msg = clientConn.getLastErrorMessage();
					ResponseMongodbPacket packet = new ResponseMongodbPacket();
					if(msg == null){
						
						packet.numberReturned = 1;
						packet.documents = new ArrayList<BSONObject>(1);
						packet.documents.add(BSON_OK);
						PACKET_LOGGER.error("cannot getLasterrorMessage with requst="+this.requestPacket);
					}else{
						packet.init(msg, conn);
					}
					packet.numberReturned = 1;
					packet.responseTo = this.requestPacket.requestID;
					
					if(PACKET_LOGGER.isDebugEnabled()){
						PACKET_LOGGER.debug("<<----@ReponsePacket="+packet+", " +clientConn.getSocketId());
					}
					clientConn.postMessage(packet.toByteBuffer(conn));
					return;
				}else{
					if(requestPacket.query.get("group") != null){
						this.cmd = MongodbPacketConstant.CMD_GROUP;
					}else if(requestPacket.query.get("count") != null){
						this.cmd = MongodbPacketConstant.CMD_COUNT;
					}else if(requestPacket.query.get("drop") != null){
						this.cmd = MongodbPacketConstant.CMD_DROP;
					}else if(requestPacket.query.get("distinct") != null){
						this.cmd = MongodbPacketConstant.CMD_DISTINCT;
					}else if(requestPacket.query.get("deleteIndexes") != null){
						this.cmd = MongodbPacketConstant.CMD_DROP_INDEXES;
					}else if(requestPacket.query.get("mapreduce") != null){
						this.cmd = MongodbPacketConstant.CMD_MAP_REDUCE;
					}else if(requestPacket.query.get("listDatabases") != null){
						this.cmd = MongodbPacketConstant.CMD_LISTDATABASES;
					}else if(requestPacket.query.get("$eval") != null){
						Object object = requestPacket.query.get("$eval");
						if(object instanceof BSONObject){
							BSONObject bson = (BSONObject) object;
							for(String name : bson.keySet()){
								if(name.startsWith("db.getSisterDB")){
									this.cmd = MongodbPacketConstant.CMD_GETCOLLECTION;
									break;
								}
							}
						}
					}
					
				}
			}
		}
		
		//other request
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
			try{
				MongodbServerConnection serverConn = (MongodbServerConnection)pool.borrowObject();
				serverConn.setSessionMessageHandler(this);
				conns[index++] = serverConn;
				handlerMap.put(serverConn, serverConn.getMessageHandler());
			}catch(Exception e){
				handlerLogger.error("poolName=["+pool.getName()+"] borrow Connection error",e);
			}
		}
		
		if(index == 0){
			throw new Exception("no pool to query,queryObject="+this.requestPacket);
		}
		
		for(MongodbServerConnection serverConn : conns){
			if(serverConn != null){
				serverConn.postMessage(message);
			}
		}
	}

	@Override
	protected void doServerResponse(MongodbServerConnection conn,
			byte[] message) {
		SimpleResponseMongodbPacket packet = null;
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		int type = MongodbPacketBuffer.getOPMessageType(message);
		
		if(type != MongodbPacketConstant.OP_REPLY){
			PACKET_LOGGER.error("unkown response packet type="+type+" , request="+this.requestPacket);
		}
		
		if(ROUTER_TRACE.isDebugEnabled() || isMulti){
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
				ResponseMongodbPacket result = null;
				if(this.cmd>0){
					FunctionMerge merge = FUNCTION_MERGE_MAP.get(this.cmd);
					result = merge.mergeResponse(this.requestPacket, multiResponsePacket);
				}else{
					if(this.requestPacket.fullCollectionName.indexOf("system.namespaces")>0){
						
						//merge name spaces
						FunctionMerge merge = FUNCTION_MERGE_MAP.get(MongodbPacketConstant.CMD_NAMESPACES);
						result = merge.mergeResponse(requestPacket, multiResponsePacket);
					}else{
						result = this.mergeResponse(this.requestPacket.numberToReturn == -1);
					}
				}
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
			
			
			if(ROUTER_TRACE.isDebugEnabled()){
				clientConn.postMessage(packet.toByteBuffer(serverConn));
			}else{
				clientConn.postMessage(message);
			}
		}
	}
}
