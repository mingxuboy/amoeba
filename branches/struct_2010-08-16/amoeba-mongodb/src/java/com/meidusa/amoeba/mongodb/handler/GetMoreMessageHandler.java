package com.meidusa.amoeba.mongodb.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.CursorEntry;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.SimpleResponseMongodbPacket;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

public class GetMoreMessageHandler extends AbstractSessionHandler<GetMoreMongodbPacket> {
	private long cursorID;
	private Map<MongodbServerConnection,CursorEntry> cursorMap = null;
	public GetMoreMessageHandler(MongodbClientConnection clientConn,
			GetMoreMongodbPacket t) {
		super(clientConn, t);
	}

	@Override
	protected void doClientRequest(MongodbClientConnection conn, byte[] message)
			throws Exception {
		List<Tuple<CursorEntry,ObjectPool>> tuples = (List<Tuple<CursorEntry,ObjectPool>>)clientConn.getCursor(cursorID);
		
		if(tuples == null){
			//TODO need to fill packet field
			ResponseMongodbPacket response = new ResponseMongodbPacket();
			clientConn.postMessage(response.toByteBuffer(clientConn));
			return;
		}
		
		this.cursorID = this.requestPacket.cursorID;
		
		if(tuples != null && tuples.size() > 1){
			isMulti = true;
			this.multiResponsePacket = new ArrayList<ResponseMongodbPacket>();
			cursorMap = new HashMap<MongodbServerConnection,CursorEntry>();
		}
		
		for(Tuple<CursorEntry,ObjectPool> tuple: tuples){
			MongodbServerConnection serverConn = (MongodbServerConnection)tuple.right.borrowObject();
			handlerMap.put(serverConn, serverConn.getMessageHandler());
			serverConn.setSessionMessageHandler(this);
			this.requestPacket.cursorID = tuple.left.cursorID;
			cursorMap.put(serverConn, tuple.left);
			serverConn.postMessage(requestPacket.toByteBuffer(this.clientConn));
		}
	}

	@Override
	protected void doServerResponse(MongodbServerConnection conn, byte[] message) {
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
		
		if(packet.cursorID <= 0){
			this.clientConn.removeCursorItem(this.cursorID,cursorMap.get(serverConn));
		}
		
		if(isMulti){
			multiResponsePacket.add((ResponseMongodbPacket)packet);

			if(endQuery(conn)){
				ResponseMongodbPacket result = mergeResponse();
				result.cursorID = cursorID;
				clientConn.postMessage(result.toByteBuffer(this.clientConn));
			}
		}else{
			endQuery(conn);
			clientConn.postMessage(message);
		}
	}

}
