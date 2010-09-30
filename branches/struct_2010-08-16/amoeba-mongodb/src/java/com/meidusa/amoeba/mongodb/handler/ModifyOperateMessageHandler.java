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

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.handler.merge.FunctionMerge;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.route.MongodbQueryRouter;
import com.meidusa.amoeba.net.poolable.ObjectPool;

public class ModifyOperateMessageHandler<T extends RequestMongodbPacket> extends AbstractSessionHandler<T> {
	private int lastRequestId = 0;
	public ModifyOperateMessageHandler(MongodbClientConnection clientConn,T t) {
		super(clientConn, t);
	}

	@Override
	protected void doClientRequest(MongodbClientConnection conn,
			byte[] message) throws Exception {

		MongodbQueryRouter router = (MongodbQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
		ObjectPool[] pools = router.doRoute(clientConn, (RequestMongodbPacket)requestPacket);
		if(pools == null || pools.length==0){
			pools = router.getDefaultObjectPool();
		}
		
		if(pools != null && pools.length >1){
			isMulti = true;
			this.multiResponsePacket = new ArrayList<ResponseMongodbPacket>();
		}
		RequestMongodbPacket lastErrorRequest = clientConn.getLastErrorRequest();
		byte[] bts = lastErrorRequest.toByteBuffer(this.clientConn).array();
		this.lastRequestId = lastErrorRequest.requestID;
		MongodbServerConnection[] conns = new MongodbServerConnection[pools.length];
		int index =0;
		for(ObjectPool pool: pools){
			MongodbServerConnection serverConn = (MongodbServerConnection)pool.borrowObject();
			handlerMap.put(serverConn, serverConn.getMessageHandler());
			serverConn.setSessionMessageHandler(this);
			conns[index++] = serverConn;
		}
		
		for(MongodbServerConnection serverConn : conns){
			if(PACKET_LOGGER.isDebugEnabled()){
				PACKET_LOGGER.debug("--->>>@errorRequestPakcet="+lastErrorRequest+"," +clientConn.getSocketId()+" send packet --->"+serverConn.getSocketId());
			}
			serverConn.postMessage(message);
			serverConn.postMessage(bts);
		}
	}

	@Override
	protected void doServerResponse(MongodbServerConnection conn,
			byte[] message) {
	
		ResponseMongodbPacket lastResponsePacket = new ResponseMongodbPacket();
		lastResponsePacket.init(message, clientConn);
		if(PACKET_LOGGER.isDebugEnabled()){
			putDebugInfoToResponsePacket(lastResponsePacket,conn);
		}
		
		if(isMulti){
			multiResponsePacket.add(lastResponsePacket);
			if(endQuery(conn)){
				FunctionMerge merge = FUNCTION_MERGE_MAP.get(MongodbPacketConstant.CMD_GETLASTERROR);
				ResponseMongodbPacket result = merge.mergeResponse(requestPacket, multiResponsePacket);
				result.responseTo = lastRequestId;
				clientConn.setLastErrorMessage(result.toByteBuffer(this.clientConn).array());
			}
		}else{
			clientConn.setLastErrorMessage(message);
			endQuery(conn);
		}
		
	}

}
