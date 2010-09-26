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
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.handler.merge.CountFunctionMerge;
import com.meidusa.amoeba.mongodb.handler.merge.DistinctFunctionMerge;
import com.meidusa.amoeba.mongodb.handler.merge.FunctionMerge;
import com.meidusa.amoeba.mongodb.handler.merge.GroupFunctionMerge;
import com.meidusa.amoeba.mongodb.handler.merge.OKFunctionMerge;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;

public abstract class AbstractSessionHandler<T extends AbstractMongodbPacket> implements SessionMessageHandler {
	public static Logger PACKET_LOGGER = Logger.getLogger("PACKETLOGGER");
	protected  static Logger handlerLogger = Logger.getLogger(AbstractSessionHandler.class);
	public static final BSONObject BSON_OK = new BasicBSONObject();
	protected static Map<Integer,FunctionMerge> FUNCTION_MERGE_MAP = new HashMap<Integer,FunctionMerge>();
	static{
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_GROUP, new GroupFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_COUNT, new CountFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_DROP, new OKFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_DROP_INDEXES, new OKFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_DISTINCT, new DistinctFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_GETLASTERROR, new OKFunctionMerge());
	}
	
	static{
		BSON_OK.put("err", null);
		BSON_OK.put("errmsg", null);
		BSON_OK.put("n", 0);
		BSON_OK.put("ok", 1.0);
	}
	protected MongodbClientConnection clientConn;
	protected Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	protected boolean isMulti = false;
	protected T requestPacket;
	protected List<ResponseMongodbPacket> multiResponsePacket = null;
	protected int cmd  = 0;
	protected boolean isFindOne = false;
	protected final long startTime = System.currentTimeMillis();
	public AbstractSessionHandler(MongodbClientConnection clientConn,T t){
		this.clientConn = clientConn;
		this.requestPacket = t;
	}
	
	@Override
	public void handleMessage(Connection conn,byte[] message) {
		
			if(conn == clientConn){
				try {
				//deserialize to packet from message
					doClientRequest((MongodbClientConnection)conn,message);
				} catch (Exception e) {
					handlerLogger.error("do client recieve message error",e);
					ResponseMongodbPacket result = new ResponseMongodbPacket();
					result.numberReturned = 1;
					result.responseFlags = 1;
					result.documents = new ArrayList<BSONObject>();
					BSONObject error = new BasicBSONObject();
					error.put("err", e.getMessage());
					error.put("n", 1);
					result.documents.add(error);
					conn.postMessage(result.toByteBuffer(conn));
				}
			}else{
				doServerResponse((MongodbServerConnection)conn,message);
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
	
	protected void putDebugInfoToResponsePacket(ResponseMongodbPacket packet,MongodbServerConnection conn){
		if(packet.numberReturned>0){
			for(BSONObject bson :packet.documents){
				bson.put("_pool_name_", conn.getObjectPool().getName());
			}
		}
		if(PACKET_LOGGER.isDebugEnabled()){
			PACKET_LOGGER.debug("<<----ReponsePacket="+packet+", requestHandler="+this.hashCode()+", " +conn.getSocketId() +"-->"+this.clientConn.getSocketId()+"\r\n");
		}
	}
	
	/**
	 * generic merge function
	 * @return
	 */
	protected ResponseMongodbPacket mergeResponse(){
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		BSONObject cmdResult = null;
		
		for(ResponseMongodbPacket response :multiResponsePacket){
			if(response.numberReturned > 0){
				if(cmd>0 || isFindOne){
					if(cmdResult == null){
						cmdResult = response.documents.get(0);
					}else{
						Number value = (Number)cmdResult.get("n");
						Number add = (Number)response.documents.get(0).get("n");
						if(value != null && add != null){
							value = value.longValue() + add.longValue(); 
							cmdResult.put("n", value.doubleValue());
						}else{
							if(add != null){
								cmdResult.put("n", add.doubleValue());
							}
						}
							
					}
				}else{
					if(result.documents == null){
						result.documents = new ArrayList<BSONObject>();
					}
					result.documents.addAll(response.documents);
				}
			}
		}
		
		result.responseTo = this.requestPacket.requestID;
		if(cmd>0 || isFindOne){
			result.documents = new ArrayList<BSONObject>();
			if(cmdResult != null){
				result.documents.add(cmdResult);
			}
		}
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
	}
	
	public boolean checkIdle(long now){
		return (now - startTime) > ProxyRuntimeContext.getInstance().getConfig().getQueryTimeout() * 1000; 
	}
}
