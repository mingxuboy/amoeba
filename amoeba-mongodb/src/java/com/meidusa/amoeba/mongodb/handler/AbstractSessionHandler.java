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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;

public abstract class AbstractSessionHandler<T extends AbstractMongodbPacket> implements SessionMessageHandler {
	protected static Logger logger = Logger.getLogger("PACKETLOGGER");
	
	protected MongodbClientConnection clientConn;
	protected Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	protected boolean isMulti = false;
	protected T requestPacket;
	protected List<ResponseMongodbPacket> multiResponsePacket = null;
	
	public AbstractSessionHandler(MongodbClientConnection clientConn,T t){
		this.clientConn = clientConn;
		this.requestPacket = t;
	}
	
	@Override
	public void handleMessage(Connection conn,byte[] message) {
		try {
			if(conn == clientConn){
				//deserialize to packet from message
				doClientRequest((MongodbClientConnection)conn,message);
			}else{
				doServerResponse((MongodbServerConnection)conn,message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void doServerResponse(MongodbServerConnection conn, byte[] message);

	protected abstract void doClientRequest(MongodbClientConnection conn, byte[] message) throws Exception;

	/**
	 * 
	 * @param conn
	 * @return boolean -- return true if all serverConnction response 
	 */
	public synchronized boolean endQuery(Connection conn){
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		serverConn.setSessionMessageHandler(null);
		serverConn.setMessageHandler(handlerMap.remove(serverConn));
		try {
			serverConn.getObjectPool().returnObject(serverConn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return handlerMap.size() == 0;
	}
	
	protected ResponseMongodbPacket mergeResponse(){
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		
		for(ResponseMongodbPacket response :multiResponsePacket){
			if(result.numberReturned > 0){
				if(result.documents == null){
					result.documents = new ArrayList<BSONObject>();
				}
				result.documents.addAll(response.documents);
			}
		}
		result.responseTo = this.requestPacket.requestID;
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
	}
}
