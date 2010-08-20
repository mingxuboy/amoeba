package com.meidusa.amoeba.mongodb.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MessageMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;

public class CommandMessageHandler implements SessionMessageHandler{
	private static Logger logger = Logger.getLogger("QueryStackLogger");
	public MessageHandler messageHandler;
	public Connection clientConn;
	public Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	
	public CommandMessageHandler(Connection clientConn){
		this.clientConn = clientConn;
	}
	
	@Override
	public void handleMessage(Connection conn,byte[] message) {
		try {
			if(conn == clientConn){
					int type = MongodbPacketBuffer.getOPMessageType(message);
					ObjectPool pool = (ObjectPool)ProxyRuntimeContext.getInstance().getPoolMap().get("server1");
					MongodbServerConnection serverConn = (MongodbServerConnection)pool.borrowObject();
					
					AbstractMongodbPacket packet = null;
					handlerMap.put(serverConn, serverConn.getMessageHandler());
					serverConn.setSessionMessageHandler(this);
					serverConn.postMessage(message);
					if(logger.isDebugEnabled()){
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
							logger.debug("--->>>"+clientConn.getSocketId()+" send packet --->"+serverConn.getSocketId()+"   "+packet);
						}else{
							logger.debug("ERROR --->>>"+clientConn.getSocketId()+" send packet --->"+serverConn.getSocketId()+"   "+packet);
						}
					}
					
					if(type == MongodbPacketConstant.OP_INSERT || type == MongodbPacketConstant.OP_UPDATE|| type == MongodbPacketConstant.OP_DELETE){
						endQuery(serverConn);
					}

			}else{
				clientConn.postMessage(message);
				if(logger.isDebugEnabled()){
					int type = MongodbPacketBuffer.getOPMessageType(message);
					if(type == MongodbPacketConstant.OP_REPLY){
						AbstractMongodbPacket packet = new ResponseMongodbPacket();
						packet.init(message, conn);
						logger.debug("<<<---receive from "+conn.getSocketId()+" -->"+clientConn.getSocketId()+"  "+packet);
					}
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
