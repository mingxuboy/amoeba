package com.meidusa.amoeba.mongodb.handler;

import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;

public class CommandMessageHandler implements MessageHandler {

	public MessageHandler messageHandler;
	public Connection clientConn;
	public Connection[] serverConns;
	public Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	
	public CommandMessageHandler(Connection clientConn,Connection ...serverConns){
		this.clientConn = clientConn;
		this.serverConns = serverConns;
		for(Connection serverConn:serverConns){
			serverConn.setMessageHandler(this);
		}
		clientConn.setMessageHandler(this);
	}
	
	@Override
	public void handleMessage(Connection conn) {
		try {
			byte[] message = null;
			if(conn == clientConn){
				while((message = conn.getInQueue().getNonBlocking()) != null){
					int type = MongodbPacketBuffer.getOPMessageType(message);
					ObjectPool pool = (ObjectPool)ProxyRuntimeContext.getInstance().getPoolMap().get("server1");
					Connection serverConn = (Connection)pool.borrowObject();
					
					handlerMap.put(serverConn, serverConn.getMessageHandler());
					serverConn.setMessageHandler(this);
					serverConn.postMessage(message);
					switch(type){
						case MongodbPacketConstant.OP_QUERY:
							break;
						case MongodbPacketConstant.OP_GET_MORE:
							break;
						case MongodbPacketConstant.OP_DELETE:
							break;
						case MongodbPacketConstant.OP_KILL_CURSORS:
							break;
						case MongodbPacketConstant.OP_UPDATE:
							break;
						case MongodbPacketConstant.OP_INSERT:
							endSession(serverConn);
							break;
						case MongodbPacketConstant.OP_MSG:
							break;
						
					}
					if(type == MongodbPacketConstant.OP_QUERY){
						AbstractMongodbPacket packet = new QueryMongodbPacket();
						packet.init(message, conn);
					}

				}
			}else{
				while((message = conn.getInQueue().getNonBlocking()) != null){
					int type = MongodbPacketBuffer.getOPMessageType(message);
					if(type == MongodbPacketConstant.OP_REPLY){
						AbstractMongodbPacket packet = new ResponseMongodbPacket();
						packet.init(message, conn);
					}
					clientConn.postMessage(message);
					endSession(conn);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void endSession(Connection conn){
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		serverConn.setMessageHandler(handlerMap.remove(serverConn));
		try {
			serverConn.getObjectPool().returnObject(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
