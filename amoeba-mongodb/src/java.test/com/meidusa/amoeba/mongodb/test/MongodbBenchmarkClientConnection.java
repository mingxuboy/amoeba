package com.meidusa.amoeba.mongodb.test;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.benchmark.AbstractBenchmarkClientConnection;
import com.meidusa.amoeba.benchmark.AbstractBenchmark.TaskRunnable;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.KillCursorsMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MessageMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.net.packet.AbstractPacket;


/**
 * 
 * @author Struct
 *
 */
public class MongodbBenchmarkClientConnection extends AbstractBenchmarkClientConnection<AbstractMongodbPacket> {
	private static Logger	logger        = Logger.getLogger(MongodbBenchmarkClientConnection.class);
	private boolean isLastModifyOperation = false;
	public MongodbBenchmarkClientConnection(SocketChannel channel, long createStamp,CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task) {
		super(channel, createStamp,requestLatcher,responseLatcher,task);
	}

	public boolean needPing(long now) {
		return false;
	}

	public boolean checkIdle(long now) {
		return false;
	}

	public AbstractMongodbPacket createPacketWithBytes(byte[] message) {
		int type = MongodbPacketBuffer.getOPMessageType(message);
		AbstractMongodbPacket packet = null;
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
			packet = new KillCursorsMongodbPacket();
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
		case MongodbPacketConstant.OP_REPLY:
			packet = new ResponseMongodbPacket();
			break;
		}
		packet.init(message, this);
		return packet;
	}

	public AbstractMongodbPacket createRequestPacket() {
		Properties properties = this.getRequestProperties();
		AbstractMongodbPacket packet = null;
		try {
			packet = (AbstractMongodbPacket)Class.forName((String)properties.get("class")).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		ParameterMapping.mappingObjectField(packet, properties,this.getContextMap(),this, AbstractPacket.class);
		return packet;
	}
	
    protected void messageProcess() {
		//_handler.handleMessage(this);
    }
    
	public void postMessage(byte[] msg) {
		postMessage(ByteBuffer.wrap(msg));
	}
	
	protected PacketInputStream createPacketInputStream() {
		return new MongodbFramedInputStream(true);
	}

	protected PacketOutputStream createPacketOutputStream() {
		return new MongodbFramingOutputStream(true);
	}
	
	public void init(){
		super.init();
	}

	@Override
	public void startBenchmark() {
		
		AbstractMongodbPacket packet = this.createRequestPacket();
		if(packet.opCode == MongodbPacketConstant.OP_DELETE 
				|| packet.opCode == MongodbPacketConstant.OP_INSERT 
				|| packet.opCode == MongodbPacketConstant.OP_UPDATE){
			isLastModifyOperation = true;
		}else{
			isLastModifyOperation = false;
		}
		postMessage(packet.toByteBuffer(this));
		
		if(isLastModifyOperation){
			postMessage(getLastErrorPacket().toByteBuffer(this));
		}
		
	}
	
	protected QueryMongodbPacket getLastErrorPacket(){
		QueryMongodbPacket packet = new QueryMongodbPacket();
		packet.fullCollectionName = "admin.$cmd";
		packet.numberToReturn = -1;
		BasicBSONObject document = new BasicBSONObject();
		document.put("getlasterror",1);
		packet.query = document;
		return packet;
	}
	
	protected void doReceiveMessage(byte[] message) {
		super.doReceiveMessage(message);
		if (responseLatcher.getCount() <= 0) {
			return;
		}
		if(isLastModifyOperation){
			postMessage(getLastErrorPacket().toByteBuffer(this));
		}
	}
}
