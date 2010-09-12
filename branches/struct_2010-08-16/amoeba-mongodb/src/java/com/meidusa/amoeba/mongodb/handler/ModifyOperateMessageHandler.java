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
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.route.MongodbQueryRouter;
import com.meidusa.amoeba.net.poolable.ObjectPool;

public class ModifyOperateMessageHandler<T extends RequestMongodbPacket> extends AbstractSessionHandler<T> {

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
		
		for(ObjectPool pool: pools){
			MongodbServerConnection serverConn = (MongodbServerConnection)pool.borrowObject();
			handlerMap.put(serverConn, serverConn.getMessageHandler());
			serverConn.setSessionMessageHandler(this);
			byte[] lastErrorRequest = clientConn.getLastErrorRequest();
			QueryMongodbPacket packet = new QueryMongodbPacket();
			packet.init(lastErrorRequest, conn);
			if(logger.isDebugEnabled()){
				logger.debug("--->>>@errorRequestPakcet="+packet+"," +clientConn.getSocketId()+" send packet --->"+serverConn.getSocketId());
			}
			clientConn.clearErrorMessage();
			serverConn.postMessage(message);
			serverConn.postMessage(lastErrorRequest);
		}
	}

	@Override
	protected void doServerResponse(MongodbServerConnection conn,
			byte[] message) {
	
		ResponseMongodbPacket lastResponsePacket = new ResponseMongodbPacket();
		lastResponsePacket.init(message, clientConn);
		if(isMulti){
			multiResponsePacket.add(lastResponsePacket);
			if(endQuery(conn)){
				
			}
			//TODO
			clientConn.lastResponsePacket = lastResponsePacket;
			clientConn.setLastErrorMessage(message);
			
		}else{
			clientConn.lastResponsePacket = lastResponsePacket;
			clientConn.setLastErrorMessage(message);
			endQuery(conn);
		}
		
	}

}
