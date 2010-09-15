package com.meidusa.amoeba.mongodb.test;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.benchmark.AbstractBenchmarkClientConnection;
import com.meidusa.amoeba.config.ConfigUtil;
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


/**
 * 
 * @author Struct
 *
 */
public class MongodbBenchmarkClientConnection extends AbstractBenchmarkClientConnection<AbstractMongodbPacket> {
	private static Logger	logger        = Logger.getLogger(MongodbBenchmarkClientConnection.class);
	private Random random = new Random();
	private Random random2 = new Random();
	final int nreturn  = Integer.parseInt(System.getProperty("return", "2"));
	final String requestFile  = System.getProperty("requestFile");
	private AtomicInteger index = new AtomicInteger();
	private boolean isLastModifyOperation = false;
	public MongodbBenchmarkClientConnection(SocketChannel channel, long createStamp,CountDownLatch latcher) {
		super(channel, createStamp,latcher);
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
	
	public AbstractMongodbPacket createRequestPacket1() {
		Properties propertis = new Properties();
		try {
			FileInputStream fis = new FileInputStream(requestFile);
			propertis.load(fis);
			AbstractMongodbPacket apacket = (AbstractMongodbPacket)Class.forName((String)propertis.get("class")).newInstance();
			//ConfigUtil.filter(text, properties)
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public AbstractMongodbPacket createRequestPacket() {
			QueryMongodbPacket packet = new QueryMongodbPacket();
			packet.fullCollectionName = "test.test";
			packet.numberToReturn = 10;//nreturn;
			//packet.returnFieldSelector = new BasicBSONObject();
			//packet.returnFieldSelector.put("s", 1);
			packet.numberToSkip = 0;
			packet.requestID = index.getAndIncrement();
			BasicBSONObject query = new BasicBSONObject();
			query.put("s", random.nextInt(10));
			packet.query = query;
			return packet;
	}
	
	public AbstractMongodbPacket createRequestPacket2() {
		GetMoreMongodbPacket packet = new GetMoreMongodbPacket();
		packet.fullCollectionName = "test.test";
		packet.numberToReturn = nreturn;
		//packet.returnFieldSelector = new BasicBSONObject();
		//packet.returnFieldSelector.put("s", 1);
		packet.requestID = index.getAndIncrement();
		packet.cursorID = 1179089230899L;
		return packet;
}
	
	public AbstractMongodbPacket createRequestPacket3() {
		InsertMongodbPacket packet = new InsertMongodbPacket();
		packet.fullCollectionName = "test.test";
		packet.documents = new ArrayList<BSONObject>();
		BasicBSONObject document = new BasicBSONObject();
		document.put("s", random.nextInt(10));
		document.put("f", random2.nextInt(100000));
		packet.documents.add(document);
		packet.requestID = index.getAndIncrement();
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
		if (latcher.getCount() <= 0) {
			return;
		}
		if(isLastModifyOperation){
			postMessage(getLastErrorPacket().toByteBuffer(this));
		}
	}
}
