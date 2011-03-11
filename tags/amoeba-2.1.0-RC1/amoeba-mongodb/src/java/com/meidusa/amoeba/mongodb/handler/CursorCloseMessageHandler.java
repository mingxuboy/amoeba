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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.CursorEntry;
import com.meidusa.amoeba.mongodb.packet.KillCursorsMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

/**
 * only for 
 */
public class CursorCloseMessageHandler implements SessionMessageHandler{
	private static Logger logger = Logger.getLogger("PACKETLOGGER");
	public Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	private String sourceClient;
	protected final long startTime = System.currentTimeMillis();
	public CursorCloseMessageHandler(String sourceClient,List<Tuple<CursorEntry,ObjectPool>> tuples){
		this.sourceClient = sourceClient;
		for(Tuple<CursorEntry,ObjectPool> tuple : tuples){
			KillCursorsMongodbPacket packet = new KillCursorsMongodbPacket();
			packet.cursorIDs = new long[]{ tuple.left.cursorID};
			packet.fullCollectionName = tuple.left.fullCollectionName;
			packet.numberOfCursorIDs = 1;
			MongodbServerConnection serverConn;
			try {
				serverConn = (MongodbServerConnection)tuple.right.borrowObject();
				handlerMap.put(serverConn, serverConn.getMessageHandler());
				serverConn.setSessionMessageHandler(this);
				serverConn.postMessage(packet.toByteBuffer(serverConn));
				if(logger.isDebugEnabled()){
					logger.debug("--->>>@CursorCloseRequestPakcet="+packet+"," +sourceClient+" send packet --->"+serverConn.getSocketId());
				}
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public void handleMessage(Connection conn, byte[] message) {
		if(logger.isDebugEnabled()){
			int type = MongodbPacketBuffer.getOPMessageType(message);
			if(type == MongodbPacketConstant.OP_REPLY){
				AbstractMongodbPacket packet = new ResponseMongodbPacket();
				packet.init(message, conn);
				logger.debug("<<<--- @CursorCloseResponsePakcet="+packet+" receive from "+conn.getSocketId()+" -->"+sourceClient);
			}
		}
		endQuery(conn);
	}

	public void endQuery(Connection conn){
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		serverConn.setSessionMessageHandler(null);
		serverConn.setMessageHandler(handlerMap.remove(serverConn));
		try {
			serverConn.getObjectPool().returnObject(serverConn);
		} catch (Exception e) {
		}
	}
	
	public boolean checkIdle(long now){
		return (now - startTime) > ProxyRuntimeContext.getInstance().getRuntimeContext().getQueryTimeout() * 1000; 
	}

	@Override
	public void forceEndSession(String cause) {
		
	}
}
