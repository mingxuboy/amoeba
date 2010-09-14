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

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.CursorEntry;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.KillCursorsMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MessageMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.SimpleResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;
import com.meidusa.amoeba.mongodb.route.MongodbQueryRouter;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

@SuppressWarnings("deprecation")
public class CommandMessageHandler implements SessionMessageHandler{
	private static Logger logger = Logger.getLogger("PACKETLOGGER");
	
	private MongodbClientConnection clientConn;
	private Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	private boolean isLastErrorRequest = false;
	private boolean isMulti = false;
	private AbstractMongodbPacket requestPacket;
	private List<ResponseMongodbPacket> multiResponsePacket = null;
	public CommandMessageHandler(MongodbClientConnection clientConn){
		this.clientConn = clientConn;
	}
	
	
	@Override
	public void handleMessage(Connection conn,byte[] message) {
		try {
			if(conn == clientConn){
				dealClientRequest((MongodbClientConnection)conn,message);
			}else{
				dealServerRequest((MongodbServerConnection)conn,message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void dealClientRequest(MongodbClientConnection conn,byte[] message) throws Exception{
			
			//deserialize to packet from message
			int type = MongodbPacketBuffer.getOPMessageType(message);
			AbstractMongodbPacket packet = null;
			switch(type){
			case MongodbPacketConstant.OP_QUERY:
				packet = new QueryMongodbPacket();
				break;
			case MongodbPacketConstant.OP_GET_MORE:
				packet = new GetMoreMongodbPacket();
				break;
			case MongodbPacketConstant.OP_DELETE:
				packet = new DeleteMongodbPacket();
				break;
			case MongodbPacketConstant.OP_KILL_CURSORS:
				packet = new KillCursorsMongodbPacket();
				break;
			case MongodbPacketConstant.OP_UPDATE:
				packet = new UpdateMongodbPacket();
				break;
			case MongodbPacketConstant.OP_INSERT:
				packet = new InsertMongodbPacket();
				break;
			case MongodbPacketConstant.OP_MSG:
				packet = new MessageMongodbPacket();
				break;
		}
		
		packet.init(message, conn);
		
		this.requestPacket = packet;
		
		//debug packet info
		if(logger.isDebugEnabled()){
			if(packet != null){
				logger.debug("--->>>pakcet="+packet+"," +clientConn.getSocketId());
			}else{
				logger.debug("ERROR --->>>"+clientConn.getSocketId()+"  unknow type="+type);
			}
		}
		
		//1. getLastError packet , end session
		if(type == MongodbPacketConstant.OP_QUERY){
			QueryMongodbPacket last = (QueryMongodbPacket) packet;
			
			if(last.fullCollectionName.indexOf("$")>0 && last.query != null 
					&& last.query.get("getlasterror") != null){
				byte[] msg = clientConn.getLastErrorMessage();
				packet = new ResponseMongodbPacket();
				packet.init(msg, conn);
				if(logger.isDebugEnabled()){
					logger.debug("<<----@ReponsePacket="+packet+", " +clientConn.getSocketId());
				}
				clientConn.postMessage(msg);
				return;
			}
		}
		
		//2. kill cursor, end session
		if(type == MongodbPacketConstant.OP_KILL_CURSORS){
			KillCursorsMongodbPacket kpacket = (KillCursorsMongodbPacket)packet;
			for(long cursorID:kpacket.cursorIDs){
				List<Tuple<CursorEntry,ObjectPool>> tupes = (List<Tuple<CursorEntry,ObjectPool>>)clientConn.removeCursor(cursorID);
				
				//start close cursor request
				if(tupes != null && tupes.size() >0){
					new CursorCloseMessageHandler(clientConn.getSocketId(),tupes);
				}
				//TODO need to fill packet field
				ResponseMongodbPacket response = new ResponseMongodbPacket();
				clientConn.postMessage(response.toByteBuffer(clientConn));
			}
			return;
		}
		
		
		
		//3. get More result from cursor , end session if no cousor in clientConnection 
		if(type == MongodbPacketConstant.OP_GET_MORE){
			GetMoreMongodbPacket getMorePacket = (GetMoreMongodbPacket)packet;
			long cursorID = getMorePacket.cursorID;
			List<Tuple<CursorEntry,ObjectPool>> tuples = (List<Tuple<CursorEntry,ObjectPool>>)clientConn.removeCursor(cursorID);
			
			if(tuples == null){
				//TODO need to fill packet field
				ResponseMongodbPacket response = new ResponseMongodbPacket();
				clientConn.postMessage(response.toByteBuffer(clientConn));
				return;
			}
			
			if(tuples != null && tuples.size() > 1){
				isMulti = true;
				this.multiResponsePacket = new ArrayList<ResponseMongodbPacket>();
			}
			
			for(Tuple<CursorEntry,ObjectPool> tuple: tuples){
				MongodbServerConnection serverConn = (MongodbServerConnection)tuple.right.borrowObject();
				handlerMap.put(serverConn, serverConn.getMessageHandler());
				serverConn.setSessionMessageHandler(this);
			}
		}
		
		//4. other request packet
		ObjectPool[] pools = null;
		MongodbQueryRouter router = (MongodbQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
		if(type == MongodbPacketConstant.OP_QUERY 
				|| type == MongodbPacketConstant.OP_DELETE 
				|| type == MongodbPacketConstant.OP_INSERT
				|| type == MongodbPacketConstant.OP_UPDATE){
			
			RequestMongodbPacket requestPacket = (RequestMongodbPacket)packet;
			pools = router.doRoute(clientConn, requestPacket);
			if(pools == null || pools.length==0){
				pools = router.getDefaultObjectPool();
			}
		}else{
			pools = router.getDefaultObjectPool();
		}
		
		if(pools != null && pools.length >1){
			isMulti = true;
			this.multiResponsePacket = new ArrayList<ResponseMongodbPacket>();
		}
		byte[] lastErrorRequest = clientConn.getLastErrorRequest().toByteBuffer(this.clientConn).array();
		for(ObjectPool pool: pools){
			MongodbServerConnection serverConn = (MongodbServerConnection)pool.borrowObject();
			handlerMap.put(serverConn, serverConn.getMessageHandler());
			serverConn.setSessionMessageHandler(this);
			
			if(type == MongodbPacketConstant.OP_INSERT  
			|| type == MongodbPacketConstant.OP_UPDATE 
			|| type == MongodbPacketConstant.OP_DELETE){
				isLastErrorRequest = true;
				
				packet = new QueryMongodbPacket();
				packet.init(lastErrorRequest, conn);
				if(logger.isDebugEnabled()){
					logger.debug("--->>>@errorRequestPakcet="+packet+"," +clientConn.getSocketId()+" send packet --->"+serverConn.getSocketId());
				}
				clientConn.clearErrorMessage();
				serverConn.postMessage(message);
				serverConn.postMessage(lastErrorRequest);
			}else{
				isLastErrorRequest = false;
				serverConn.postMessage(message);
			}
		}
	}
	
	protected void dealServerRequest(MongodbServerConnection conn,byte[] message){
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
			logger.debug("<<<--- "+(isLastErrorRequest?"@errorReponse":"Reponse")+"pakcet="+packet+" receive from "+conn.getSocketId()+" -->"+clientConn.getSocketId());
		}
		
		if(isMulti){
			this.multiResponsePacket.add((ResponseMongodbPacket)packet);
			if(endQuery(conn)){
				
			}
		}else{
			
			if(!isLastErrorRequest){
				
				//remove cursor where clientConnection stored if no cursorID  and last request is getMorePacket
				if(packet.cursorID >0){
					if(!(this.requestPacket instanceof GetMoreMongodbPacket)){
						CursorEntry entry = new CursorEntry();
						entry.cursorID = packet.cursorID;
						
						entry.fullCollectionName = ((QueryMongodbPacket)this.requestPacket).fullCollectionName;
						
						Tuple<CursorEntry,ObjectPool> tuple = new Tuple<CursorEntry,ObjectPool>(entry,serverConn.getObjectPool());
						clientConn.addCursorItem(packet.cursorID, tuple);
					}
				}else{
					if((this.requestPacket instanceof GetMoreMongodbPacket)){
						GetMoreMongodbPacket getMorePacket = (GetMoreMongodbPacket)this.requestPacket;
						clientConn.removeCursor(getMorePacket.cursorID);
					}
				}
				clientConn.postMessage(message);
			}else{
				ResponseMongodbPacket lastResponsePacket = new ResponseMongodbPacket();
				lastResponsePacket.init(message, clientConn);
				clientConn.setLastErrorMessage(message);
			}
			endQuery(conn);
		}
		
		if(packet.cursorID >0){
			List<Tuple<CursorEntry,ObjectPool>> tupes = (List<Tuple<CursorEntry,ObjectPool>>)this.clientConn.getCursor(packet.cursorID);
			if(tupes == null){
			}
		}
	}

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

}
