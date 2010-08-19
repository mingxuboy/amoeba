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
import com.meidusa.amoeba.net.poolable.ObjectPool;

public class CommandMessageHandler implements MessageHandler{
	private static Logger logger = Logger.getLogger(CommandMessageHandler.class);
	public MessageHandler messageHandler;
	public Connection clientConn;
	public Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	
	public CommandMessageHandler(Connection clientConn){
		this.clientConn = clientConn;
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
					
					AbstractMongodbPacket packet = null;
					handlerMap.put(serverConn, serverConn.getMessageHandler());
					serverConn.setMessageHandler(this);
					serverConn.postMessage(message);
					switch(type){
						case MongodbPacketConstant.OP_QUERY:
							packet = new QueryMongodbPacket();
							break;
						case MongodbPacketConstant.OP_GET_MORE:
							packet = new GetMoreMongodbPacket();
							break;
						case MongodbPacketConstant.OP_DELETE:
							packet = new DeleteMongodbPacket();
							endQuery(serverConn);
							break;
						case MongodbPacketConstant.OP_KILL_CURSORS:
							break;
						case MongodbPacketConstant.OP_UPDATE:
							packet = new UpdateMongodbPacket();
							endQuery(serverConn);
							break;
						case MongodbPacketConstant.OP_INSERT:
							packet = new InsertMongodbPacket();
							endQuery(serverConn);
							break;
						case MongodbPacketConstant.OP_MSG:
							packet = new MessageMongodbPacket();
							break;
						
					}
					
					if(logger.isDebugEnabled()){
						if(packet != null){
							packet.init(message, conn);
							logger.debug("--->>>"+clientConn.getSocketId()+" send packet --->"+serverConn.getSocketId()+"\n"+packet);
						}
					}

				}
			}else{
				while((message = conn.getInQueue().getNonBlocking()) != null){
					if(logger.isDebugEnabled()){
						int type = MongodbPacketBuffer.getOPMessageType(message);
						if(type == MongodbPacketConstant.OP_REPLY){
							AbstractMongodbPacket packet = new ResponseMongodbPacket();
							packet.init(message, conn);
							logger.debug("<<<---receive from "+conn.getSocketId()+" -->"+clientConn.getSocketId()+" \n"+packet);
						}
					}
					clientConn.postMessage(message);
					endQuery(conn);
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void endQuery(Connection conn){
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		serverConn.setMessageHandler(handlerMap.remove(serverConn));
		try {
			serverConn.getObjectPool().returnObject(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
