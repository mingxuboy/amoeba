package com.meidusa.amoeba.mongodb.net;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.BasicBSONObject;



import com.meidusa.amoeba.mongodb.handler.CommandMessageHandler;
import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;

public class MongodbClientConnection extends AbstractMongodbConnection{
	static byte[] LASTERROR = null;
	public ResponseMongodbPacket lastResponsePacket = null;
	private LinkedBlockingQueue<byte[]> lastErrorQueue = new LinkedBlockingQueue<byte[]>(1);
	private AtomicInteger requestId = new AtomicInteger(0);

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

	public void clearErrorMessage(){
		lastErrorQueue.clear();
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
		CommandMessageHandler handler =	new CommandMessageHandler(this);
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
