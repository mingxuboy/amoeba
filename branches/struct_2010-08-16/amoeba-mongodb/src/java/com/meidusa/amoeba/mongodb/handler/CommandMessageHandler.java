package com.meidusa.amoeba.mongodb.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.KillCurosorsMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MessageMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;
import com.meidusa.amoeba.mongodb.route.MongodbQueryRouter;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.route.QueryRouter;

@SuppressWarnings("deprecation")
public class CommandMessageHandler implements SessionMessageHandler{
	private static Logger logger = Logger.getLogger("PACKETLOGGER");
	
	public MessageHandler messageHandler;
	public MongodbClientConnection clientConn;
	public Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	private boolean isLastErrorRequest = false;
	public CommandMessageHandler(MongodbClientConnection clientConn){
		this.clientConn = clientConn;
	}
	
	@Override
	public void handleMessage(Connection conn,byte[] message) {
		try {
			if(conn == clientConn){
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
					packet = new KillCurosorsMongodbPacket();
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
		
			if(packet != null){
				packet.init(message, conn);
			}
			if(logger.isDebugEnabled()){
				if(packet != null){
					logger.debug("--->>>pakcet="+packet+"," +clientConn.getSocketId());
				}else{
					logger.debug("ERROR --->>>"+clientConn.getSocketId()+"  unknow type="+type);
				}
			}
			if(type == MongodbPacketConstant.OP_QUERY){
				if(packet == null){
					packet = new QueryMongodbPacket();
					packet.init(message, conn);
				}
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
			
			for(ObjectPool pool: pools){
				MongodbServerConnection serverConn = (MongodbServerConnection)pool.borrowObject();
				handlerMap.put(serverConn, serverConn.getMessageHandler());
				serverConn.setSessionMessageHandler(this);
				
				if(type == MongodbPacketConstant.OP_INSERT  
				|| type == MongodbPacketConstant.OP_UPDATE 
				|| type == MongodbPacketConstant.OP_DELETE){
					isLastErrorRequest = true;
					byte[] msg = clientConn.getLastErrorRequest();
					packet = new QueryMongodbPacket();
					packet.init(msg, conn);
					if(logger.isDebugEnabled()){
						logger.debug("--->>>@errorRequestPakcet="+packet+"," +clientConn.getSocketId()+" send packet --->"+serverConn.getSocketId());
					}
					clientConn.clearErrorMessage();
					serverConn.postMessage(message);
					serverConn.postMessage(msg);
					//endQuery(serverConn);
				}else{
					isLastErrorRequest = false;
					serverConn.postMessage(message);
				}
			}

	}else{
		if(logger.isDebugEnabled()){
			int type = MongodbPacketBuffer.getOPMessageType(message);
			if(type == MongodbPacketConstant.OP_REPLY){
				AbstractMongodbPacket packet = new ResponseMongodbPacket();
				packet.init(message, conn);
				logger.debug("<<<--- "+(isLastErrorRequest?"@errorReponse":"Reponse")+"pakcet="+packet+" receive from "+conn.getSocketId()+" -->"+clientConn.getSocketId());
			}
		}
		
		if(!isLastErrorRequest){
			clientConn.postMessage(message);
		}else{
			clientConn.lastResponsePacket = new ResponseMongodbPacket();
			clientConn.lastResponsePacket.init(message, clientConn);
			clientConn.setLastErrorMessage(message);
		}
		
		endQuery(conn);
		
	}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void endQuery(Connection conn){
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		serverConn.setSessionMessageHandler(null);
		serverConn.setMessageHandler(handlerMap.remove(serverConn));
		try {
			serverConn.getObjectPool().returnObject(serverConn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*if(logger.isDebugEnabled()){
			QueryStackLogger.flushLog(this);
		}*/
	}

}
