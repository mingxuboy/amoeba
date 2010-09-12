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
package com.meidusa.amoeba.mongodb.net;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.mongodb.handler.AbstractSessionHandler;
import com.meidusa.amoeba.mongodb.handler.CursorCloseMessageHandler;
import com.meidusa.amoeba.mongodb.handler.QueryMessageHandler;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.CursorEntry;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.KillCurosorsMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MessageMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

/**
 * 
 * @author struct
 *
 */
@SuppressWarnings("unchecked")
public class MongodbClientConnection extends AbstractMongodbConnection{
	private static Logger logger = Logger.getLogger("PACKETLOGGER");
	static byte[] LASTERROR = null;
	public ResponseMongodbPacket lastResponsePacket = null;
	private LinkedBlockingQueue<byte[]> lastErrorQueue = new LinkedBlockingQueue<byte[]>(1);
	private AtomicInteger requestId = new AtomicInteger(0);
	private AtomicLong currentCursorID = new AtomicLong(0x10001L);
	private LRUMap cursorMap = new LRUMap(10){
		private static final long serialVersionUID = 1L;
		protected boolean removeLRU(LinkEntry entry){
			boolean result = super.removeLRU(entry);
			List<Tuple<CursorEntry,ObjectPool>> tupleList = (List<Tuple<CursorEntry,ObjectPool>>)entry.getValue();
			if(tupleList.size()>0){
				new CursorCloseMessageHandler(MongodbClientConnection.this.getSocketId(),tupleList);
			}
			return result;
		}
	};
	
	//private Map<Long,Tuple<Long,ObjectPool>> cursorMap = new HashMap<Long,Tuple<Long,ObjectPool>>(2);
	
	private  QueryMongodbPacket LastErrorPacket = new QueryMongodbPacket();
	{
		LastErrorPacket.query = new BasicBSONObject();
		LastErrorPacket.numberToReturn = -1;
		LastErrorPacket.fullCollectionName="admin.$cmd";
		LastErrorPacket.query.put("getlasterror", 1);
	}
	
	static{
		ResponseMongodbPacket temp =  new ResponseMongodbPacket();
		LASTERROR = temp.toByteBuffer(null).array();
		//LastErrorResponsePacket.documents = new ArrayList();
	}
	
	public byte[] lastErrorMessage = LASTERROR;
	
	public byte[] getLastErrorMessage() {
		try {
			return lastErrorQueue.poll(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return LASTERROR;
		}
	}

	public long nextCursorID(){
		return currentCursorID.incrementAndGet();
	}
	
	public void clearErrorMessage(){
		lastErrorQueue.clear();
	}
	
	public void putCursor(long cursorID,List<Tuple<CursorEntry,ObjectPool>> tuples){
		synchronized (cursorMap) {
			cursorMap.put(cursorID, tuples);
		}
	}
	public void addCursorItem(long cursorID,Tuple<CursorEntry,ObjectPool> tuple){
		synchronized (cursorMap) {
			List<Tuple<CursorEntry,ObjectPool>> tuples = (List<Tuple<CursorEntry,ObjectPool>>)cursorMap.get(cursorID);
			if(tuples == null){
				List<Tuple<CursorEntry,ObjectPool>> newList = new ArrayList<Tuple<CursorEntry,ObjectPool>>();
				newList.add(tuple);
				cursorMap.put(cursorID, newList);
			}else{
				_addNew_:{
					for(Tuple<CursorEntry,ObjectPool> storedTuple : tuples){
						if(storedTuple.left.equals(tuple.left)){
							break _addNew_;
						}
					}
					tuples.add(tuple);
				}
			}
		}
	}
	
	public void removeCursorItem(long cursorID,CursorEntry cursorEntry){
		synchronized (cursorMap) {
			List<Tuple<CursorEntry,ObjectPool>> tuples = (List<Tuple<CursorEntry,ObjectPool>>)cursorMap.get(cursorID);
			if(tuples != null){
				_addNew_:{
					for(Tuple<CursorEntry,ObjectPool> storedTuple : tuples){
						if(storedTuple.left.equals(cursorEntry)){
							tuples.remove(storedTuple);
							if(tuples.size() == 0){
								cursorMap.remove(cursorID);
							}
							break _addNew_;
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Tuple<CursorEntry,ObjectPool>> removeCursor(long cursorID){
		synchronized (cursorMap) {
			return (List<Tuple<CursorEntry,ObjectPool>>) cursorMap.remove(cursorID);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Tuple<CursorEntry,ObjectPool>> getCursor(long cursorID){
		synchronized (cursorMap) {
			return (List<Tuple<CursorEntry,ObjectPool>>)cursorMap.get(cursorID);
		}
	}
	
	public synchronized byte[] getLastErrorRequest(){
		LastErrorPacket.requestID = requestId.incrementAndGet();
		return LastErrorPacket.toByteBuffer(this).array();
	}
	
	public void setLastErrorMessage(byte[] lastErrorMessage) {
		if(requestId.get() == MongodbPacketBuffer.getResponseId(lastErrorMessage)){
			lastErrorQueue.clear();
			lastErrorQueue.offer(lastErrorMessage);
		}
	}

	public MongodbClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
		//this.setMessageHandler(new CommandMessageHandler(this));
	}

	protected synchronized void doReceiveMessage(byte[] message){
		int type = MongodbPacketBuffer.getOPMessageType(message);
		AbstractMongodbPacket packet = null;
		AbstractSessionHandler<?> handler = null;
		switch(type){
		case MongodbPacketConstant.OP_QUERY:
			packet = new QueryMongodbPacket();
			handler = new QueryMessageHandler(this,(QueryMongodbPacket)packet);
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
		packet.init(message, this);
		
		//debug packet info
		if(logger.isDebugEnabled()){
			if(packet != null){
				logger.debug("--->>>pakcet="+packet+"," +this.getSocketId());
			}else{
				logger.debug("ERROR --->>>"+this.getSocketId()+"  unknow type="+type);
			}
		}
		handler.handleMessage(this,message);
	}
	
	/*@Override
	public void handleMessage(Connection conn) {
		try {
			new CommandMessageHandler(this).handleMessage(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	/*protected void messageProcess() {
		byte[] message = null;
		while((message = getInQueue().getNonBlocking()) != null){
			CommandMessageHandler handler =	new CommandMessageHandler(this);
			handler.handleMessage(this,message);
		}
    }*/

}
