package com.meidusa.amoeba.mongodb.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.map.LRUMap;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.route.MongodbQueryRouter;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

public class AmoebaSequenceHandler extends QueryMessageHandler {
	private static Map SEQUENCE_MAP = Collections.synchronizedMap(new LRUMap(50000));
	private static long SIZE = 1000;
	private static String SEQ_NAME="seq_name";
	private static String VALUE = "value";
	private static String NEXT = "$next";
	private String key = null;
	private boolean nextValue = false;
	private static boolean inProgress = false;
	public AmoebaSequenceHandler(MongodbClientConnection clientConn,
			QueryMongodbPacket t) {
		super(clientConn, t);
	}

	@Override
	protected void doClientRequest(MongodbClientConnection conn, byte[] message)
			throws Exception {
		if(this.requestPacket.query != null){
			key = (String)this.requestPacket.query.get(SEQ_NAME);
		}
		
		if(this.requestPacket.returnFieldSelector != null){
			Object n = (Object)this.requestPacket.returnFieldSelector.get(NEXT);
			if(n != null){
				nextValue = true;
			}
		}
		
		if(key == null || !nextValue){
			super.doClientRequest(conn, message);
			return;
		}else{
			Tuple<Boolean,Long> nextSequenceTuple = getNextSequenceTuple(key);
			int time = 0;
			if(nextSequenceTuple.left){
				do{
					synchronized (key.intern()) {
						nextSequenceTuple = getNextSequenceTuple(key);
						
						if(!nextSequenceTuple.left){
							break;
						}
						if(!inProgress){
							inProgress = true;
							
							QueryMongodbPacket packet = this.getSequenceRequest(key);
							//other request
							MongodbQueryRouter router = (MongodbQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
	
							ObjectPool[] pools = router.doRoute(clientConn, requestPacket);
							if(pools == null || pools.length==0){
								pools = router.getDefaultObjectPool();
							}
							
							if(pools != null && pools.length >1){
								isMulti = true;
								this.multiResponsePacket = new ArrayList<ResponseMongodbPacket>();
							}
							
							MongodbServerConnection[] conns = new MongodbServerConnection[pools.length];
							int index =0;
							for(ObjectPool pool: pools){
								try{
									MongodbServerConnection serverConn = (MongodbServerConnection)pool.borrowObject();
									serverConn.setSessionMessageHandler(this);
									conns[index++] = serverConn;
									handlerMap.put(serverConn, serverConn.getMessageHandler());
								}catch(Exception e){
									handlerLogger.error("poolName=["+pool.getName()+"] borrow Connection error",e);
								}
							}
							
							if(index == 0){
								throw new Exception("no pool to query,queryObject="+this.requestPacket);
							}
							
							for(MongodbServerConnection serverConn : conns){
								if(serverConn != null){
									serverConn.postMessage(packet.toByteBuffer(serverConn));
								}
							}
						}
						key.intern().wait(2*1000);
						nextSequenceTuple =  getNextSequenceTuple(key);
					}
					time ++;
					if(nextSequenceTuple.left && time >5){
						ResponseMongodbPacket result = new ResponseMongodbPacket();
						result.numberReturned = 1;
						result.responseFlags = 1;
						result.documents = new ArrayList<BSONObject>();
						BSONObject error = new BasicBSONObject();
						error.put("err", "SEQUENCE key not found");
						error.put("errmsg", "SEQUENCE key not found");
						error.put("ok", 0.0);
						error.put("n", 1);
						result.documents.add(error);
						result.responseTo = requestPacket.requestID;
						conn.postMessage(result.toByteBuffer(conn));
						return;
					}
				}while(nextSequenceTuple.left);
				conn.postMessage(createResponse(nextSequenceTuple.right).toByteBuffer(conn));
				
			}else{
				conn.postMessage(createResponse(nextSequenceTuple.right).toByteBuffer(conn));
			}
			
		}
	}

	private Tuple<Boolean,Long> getNextSequenceTuple(String key){
		boolean need = false;
		long number = 0;
		Tuple<Long,AtomicLong> tuple = (Tuple<Long,AtomicLong>)SEQUENCE_MAP.get(key);
		if(tuple == null){
			need = true;
		}else{
			number = tuple.right.addAndGet(1);
			if(number > tuple.left+SIZE){
				need = true;
			}else{
				need = false;
			}
		}
		
		return new Tuple<Boolean,Long>(need,number);
	}
	
	@Override
	protected void doServerResponse(MongodbServerConnection conn, byte[] message) {
		if(key == null || !nextValue){
			super.doServerResponse(conn, message);
			return;
		}
		
		ResponseMongodbPacket packet = new ResponseMongodbPacket();
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		int type = MongodbPacketBuffer.getOPMessageType(message);
		
		if(type != MongodbPacketConstant.OP_REPLY){
			PACKET_LOGGER.error("unkown response packet type="+type+" , request="+this.requestPacket);
		}
		packet.init(message, conn);
		
		if(PACKET_LOGGER.isDebugEnabled()){
			PACKET_LOGGER.debug("<<---["+this.requestPacket.requestID+"]--pakcet="+packet+"," +conn.getSocketId());
		}
		
		Tuple<Long,AtomicLong> tuple = null;
		synchronized (key.intern()){
			if(packet.documents != null && packet.documents.size() >0){
				 BSONObject  bsValue = (BSONObject)packet.documents.get(0).get(VALUE);
				 Object object = bsValue.get(VALUE);
				 long value = 0;
				 if(object != null){
					 value = (Long)object;
				 }
				 tuple = (Tuple<Long,AtomicLong>)SEQUENCE_MAP.get(key);
				 if(tuple == null){
					 tuple = new Tuple<Long,AtomicLong>(value,new AtomicLong(value));
					 SEQUENCE_MAP.put(key, tuple);
				 }else{
					 tuple.right.set(value);
					 tuple.left = value;
				 }
			}else{
				tuple = new Tuple<Long,AtomicLong>(0L,new AtomicLong(0));
				SEQUENCE_MAP.put(key, tuple);
			}
			inProgress = false;
			key.intern().notifyAll();
		}
		endQuery(conn);
		
	}
	
	private ResponseMongodbPacket createResponse(long number){
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		result.numberReturned = 1;
		result.responseFlags = 0;
		result.documents = new ArrayList<BSONObject>();
		BSONObject value = new BasicBSONObject();
		
		if(this.requestPacket.returnFieldSelector == null || this.requestPacket.returnFieldSelector.toMap().size() ==0){
			value.put(NEXT, number);
			value.put(SEQ_NAME, key);
		}else{
			if(this.requestPacket.returnFieldSelector.containsField(VALUE)){
				value.put(VALUE, number);
			}
			
			if(this.requestPacket.returnFieldSelector.containsField(NEXT)){
				value.put(NEXT, number);
			}
			
			if(this.requestPacket.returnFieldSelector.containsField(SEQ_NAME)){
				value.put(SEQ_NAME, key);
			}
		}
		result.documents.add(value);
		result.responseTo = requestPacket.requestID;
		return result;
	}
	public synchronized QueryMongodbPacket getSequenceRequest(String key){
		QueryMongodbPacket packet = new QueryMongodbPacket();
		packet.query = new BasicBSONObject();
		packet.numberToReturn = -1;
		packet.fullCollectionName = "AMOEBA.$cmd";
		
		//findandmodify
		packet.query.put("findandmodify", "SEQUENCE");
		
		//query
		BSONObject queryKey = new BasicBSONObject();
		queryKey.put(SEQ_NAME, key);
		
		packet.query.put("query",queryKey);
		
		//update
		BSONObject update = new BasicBSONObject();
		BSONObject value = new BasicBSONObject();
		value.put(VALUE, SIZE);
		update.put("$inc", value);
		
		BSONObject set = new BasicBSONObject();
		set.put(SEQ_NAME, key);
		update.put("$set", set);
		
		packet.query.put("update",update);
		
		BSONObject returnFieldSelector = null;
		if(this.requestPacket.returnFieldSelector !=null){
			returnFieldSelector = new BasicBSONObject();
			returnFieldSelector.putAll(this.requestPacket.returnFieldSelector);
			returnFieldSelector.removeField(NEXT);
			returnFieldSelector.put(VALUE, 1);
		}
		
		packet.query.put("fields", returnFieldSelector);
		//upsert
		packet.query.put("upsert", true);
		
		packet.requestID = this.clientConn.getNextRequestId();
		return packet;
	}

}
