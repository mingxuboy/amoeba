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
package com.meidusa.amoeba.mongodb.net;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mongodb.handler.AbstractSessionHandler;
import com.meidusa.amoeba.mongodb.handler.AmoebaSequenceHandler;
import com.meidusa.amoeba.mongodb.handler.CursorCloseMessageHandler;
import com.meidusa.amoeba.mongodb.handler.DeleteMessageHandler;
import com.meidusa.amoeba.mongodb.handler.GetMoreMessageHandler;
import com.meidusa.amoeba.mongodb.handler.InsertMessageHandler;
import com.meidusa.amoeba.mongodb.handler.KillCursorMessageHandler;
import com.meidusa.amoeba.mongodb.handler.QueryMessageHandler;
import com.meidusa.amoeba.mongodb.handler.UpdateMessageHandler;
import com.meidusa.amoeba.mongodb.interceptor.PacketInterceptor;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
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
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

/**
 * 
 * @author struct
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class MongodbClientConnection extends AbstractMongodbConnection{
	protected  static Logger logger = Logger.getLogger(MongodbClientConnection.class);
	private LinkedBlockingQueue<byte[]> lastErrorQueue = new LinkedBlockingQueue<byte[]>(1);
	private Map<String,PacketInterceptor<AbstractMongodbPacket>> interceptors;
	private AtomicInteger LastErrorrequestId = new AtomicInteger(0);
	private AtomicInteger sequeceRequestId = new AtomicInteger(0);
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
	
	public byte[] getLastErrorMessage() {
		try {
			do{
				byte[] byts = lastErrorQueue.poll(5, TimeUnit.SECONDS);
				if(byts == null){
					return null;
				}
				if(LastErrorrequestId.get() == MongodbPacketBuffer.getResponseId(byts)){
					return byts;
				}
			}while(true);
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	public void setLastErrorMessage(byte[] lastErrorMessage) {
		int lastId = LastErrorrequestId.get();
		int lastResponseId = MongodbPacketBuffer.getResponseId(lastErrorMessage);
		if(lastId == lastResponseId){
			lastErrorQueue.clear();
			lastErrorQueue.offer(lastErrorMessage);
		}else{
			logger.warn("last ErrorMessage not fit: lastId="+lastId+", lastResponse="+lastResponseId);
		}
	}
	
	public long nextCursorID(){
		return currentCursorID.incrementAndGet();
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
				for(Tuple<CursorEntry,ObjectPool> storedTuple : tuples){
					if(storedTuple.left.equals(tuple.left)){
						return;
					}
				}
				tuples.add(tuple);
			}
		}
	}
	
	public void removeCursorItem(long cursorID,CursorEntry cursorEntry){
		synchronized (cursorMap) {
			List<Tuple<CursorEntry,ObjectPool>> tuples = (List<Tuple<CursorEntry,ObjectPool>>)cursorMap.get(cursorID);
			if(tuples != null){
				for(Tuple<CursorEntry,ObjectPool> storedTuple : tuples){
					if(storedTuple.left.equals(cursorEntry)){
						tuples.remove(storedTuple);
						if(tuples.size() == 0){
							cursorMap.remove(cursorID);
						}
						break;
					}
				}
			}
		}
	}
	
	public List<Tuple<CursorEntry,ObjectPool>> removeCursor(long cursorID){
		synchronized (cursorMap) {
			return (List<Tuple<CursorEntry,ObjectPool>>) cursorMap.remove(cursorID);
		}
	}
	
	public List<Tuple<CursorEntry,ObjectPool>> getCursor(long cursorID){
		synchronized (cursorMap) {
			return (List<Tuple<CursorEntry,ObjectPool>>)cursorMap.get(cursorID);
		}
	}
	
	public synchronized RequestMongodbPacket getLastErrorRequest(){
		QueryMongodbPacket packet = new QueryMongodbPacket();
		packet.query = new BasicBSONObject();
		packet.numberToReturn = -1;
		packet.fullCollectionName = "admin.$cmd";
		packet.query.put("getlasterror", 1);
		packet.requestID = LastErrorrequestId.incrementAndGet();
		return packet;
	}
	
	public int getNextRequestId(){
		return sequeceRequestId.incrementAndGet();
	}

	public Map<String, PacketInterceptor<AbstractMongodbPacket>> getInterceptors() {
		return interceptors;
	}

	public void setInterceptors(
			Map<String, PacketInterceptor<AbstractMongodbPacket>> interceptors) {
		this.interceptors = interceptors;
	}

	public MongodbClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	protected void doReceiveMessage(final byte[] message){
		if(ProxyRuntimeContext.getInstance().getRuntimeContext().isUseMultipleThread()){
			ProxyRuntimeContext.getInstance().getRuntimeContext().getClientSideExecutor().execute(new Runnable(){
				public void run(){
					doReceiveMessage0(message);
				}
			});
		}else{
			doReceiveMessage0(message);
		}
	}
	
	private void doReceiveMessage0(byte[] message){
		
		int type = MongodbPacketBuffer.getOPMessageType(message);
		AbstractMongodbPacket packet = null;
		AbstractSessionHandler<?> handler = null;
		switch(type){
		case MongodbPacketConstant.OP_QUERY:
			packet = new QueryMongodbPacket();
			packet.init(message, this);
			if(MongodbPacketConstant.AMOEBA_SEQUENCE.equalsIgnoreCase(((QueryMongodbPacket)packet).fullCollectionName)){
				handler = new AmoebaSequenceHandler(this,(QueryMongodbPacket)packet);
			}else{
				handler = new QueryMessageHandler(this,(QueryMongodbPacket)packet);
			}
			break;
		case MongodbPacketConstant.OP_GET_MORE:
			packet = new GetMoreMongodbPacket();
			packet.init(message, this);
			handler = new GetMoreMessageHandler(this,(GetMoreMongodbPacket)packet);
			break;
		case MongodbPacketConstant.OP_DELETE:
			packet = new DeleteMongodbPacket();
			packet.init(message, this);
			handler = new DeleteMessageHandler(this,(DeleteMongodbPacket)packet);
			break;
		case MongodbPacketConstant.OP_KILL_CURSORS:
			packet = new KillCursorsMongodbPacket();
			packet.init(message, this);
			handler = new KillCursorMessageHandler(this,(KillCursorsMongodbPacket)packet);
			break;
		case MongodbPacketConstant.OP_UPDATE:
			packet = new UpdateMongodbPacket();
			packet.init(message, this);
			handler = new UpdateMessageHandler(this,(UpdateMongodbPacket)packet);
			break;
		case MongodbPacketConstant.OP_INSERT:
			packet = new InsertMongodbPacket();
			packet.init(message, this);
			handler = new InsertMessageHandler(this,(InsertMongodbPacket)packet);
			break;
		case MongodbPacketConstant.OP_MSG:
			packet = new MessageMongodbPacket();
			packet.init(message, this);
			break;
		}
		
		//debug packet info
		if(AbstractSessionHandler.PACKET_LOGGER.isDebugEnabled()){
			if(packet != null){
				AbstractSessionHandler.PACKET_LOGGER.debug(">>>---["+packet.requestID+"]--pakcet="+packet+"," +this.getSocketId());
			}else{
				AbstractSessionHandler.PACKET_LOGGER.debug("ERROR --->>>"+this.getSocketId()+"  unknow type="+type);
			}
		}
		
		if(this.interceptors != null){
			PacketInterceptor<AbstractMongodbPacket> interceptor = interceptors.get(packet.getClass().getName());
			if(interceptor != null){
				if(interceptor.doIntercept(packet)){
					message = packet.toByteBuffer(this).array();
				}
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
