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
import com.meidusa.amoeba.mongodb.handler.merge.GetCollectionFunctionMerge;
import com.meidusa.amoeba.mongodb.handler.merge.GroupFunctionMerge;
import com.meidusa.amoeba.mongodb.handler.merge.ListDBFunctionMerge;
import com.meidusa.amoeba.mongodb.handler.merge.NameSpacesFunctionMerge;
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
	public static Logger PACKET_LOGGER = Logger.getLogger("PACKET_LOGGER");
	public static Logger ROUTER_TRACE = Logger.getLogger("ROUTER_TRACE");
	protected  static Logger handlerLogger = Logger.getLogger(AbstractSessionHandler.class);
	public static final BSONObject BSON_OK = new BasicBSONObject();
	protected static Map<Integer,FunctionMerge> FUNCTION_MERGE_MAP = new HashMap<Integer,FunctionMerge>();
	static{
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_COUNT, new CountFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_GROUP, new GroupFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_DROP, new OKFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_DROP_INDEXES, new OKFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_DISTINCT, new DistinctFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_GETLASTERROR, new OKFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_LISTDATABASES, new ListDBFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_GETCOLLECTION,new GetCollectionFunctionMerge());
		FUNCTION_MERGE_MAP.put(MongodbPacketConstant.CMD_NAMESPACES,new NameSpacesFunctionMerge());
		
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
	protected final long startTime = System.currentTimeMillis();
	protected boolean isEnd = false;
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
					error.put("errmsg", e.getMessage());
					error.put("ok", 0.0);
					error.put("n", 1);
					result.documents.add(error);
					result.responseTo = requestPacket.requestID;
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
		if(isEnd) return true;
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		serverConn.setSessionMessageHandler(null);
		serverConn.setMessageHandler(handlerMap.remove(serverConn));
		try {
			serverConn.getObjectPool().returnObject(serverConn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isEnd = (handlerMap.size() == 0);
	}
	
	protected void putDebugInfoToResponsePacket(ResponseMongodbPacket packet,MongodbServerConnection conn){
		if(packet.numberReturned>0){
			for(BSONObject bson :packet.documents){
				bson.put("_pool_name_", conn.getObjectPool().getName()+"@"+conn.getSocketId());
			}
		}
	}
	
	/**
	 * generic merge function
	 * @return
	 */
	protected ResponseMongodbPacket mergeResponse(boolean isFindOne){
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		result.responseTo = this.requestPacket.requestID;
		BSONObject cmdResult = null;
		if(cmd>0 || isFindOne){
			for(ResponseMongodbPacket response :multiResponsePacket){
				if(response.numberReturned > 0){
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
				}
			}
			result.documents = new ArrayList<BSONObject>();
			if(cmdResult != null){
				result.documents.add(cmdResult);
			}
		}else{
			for(ResponseMongodbPacket response :multiResponsePacket){
				if(response.numberReturned > 0){
					if(result.documents == null){
						result.documents = new ArrayList<BSONObject>();
					}
					result.documents.addAll(response.documents);
				}
			}
		}

		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
	}
	
	public boolean checkIdle(long now){
		if(isEnd){
			return true;
		}

		if(ProxyRuntimeContext.getInstance().getRuntimeContext().getQueryTimeout() >0){
			return (now - startTime) > ProxyRuntimeContext.getInstance().getRuntimeContext().getQueryTimeout() * 1000;
		}else{
			return false;
		}
	}
	
	protected void closeAllServerConnection(){
		for(Connection conn : this.handlerMap.keySet()){
			if(conn instanceof MongodbServerConnection){
				((MongodbServerConnection) conn).close(new Exception()); 
			}
		}
	}
	
	public synchronized void forceEndSession(String cause){
		if(isEnd){
			return;
		}
		closeAllServerConnection();
		BSONObject errObject = new BasicBSONObject();
		errObject.put("err", cause);
		errObject.put("errmsg", cause);
		errObject.put("n", 0);
		errObject.put("ok", 0.0);
		ResponseMongodbPacket packet = new ResponseMongodbPacket();
		packet.numberReturned = 1;
		packet.documents = new ArrayList<BSONObject>(1);
		packet.documents.add(errObject);
		packet.responseTo = requestPacket.requestID;
		this.clientConn.postMessage(packet.toByteBuffer(this.clientConn));
	}
}
