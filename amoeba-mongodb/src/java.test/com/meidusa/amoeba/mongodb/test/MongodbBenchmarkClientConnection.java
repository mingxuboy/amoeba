package com.meidusa.amoeba.mongodb.test;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.benchmark.AbstractBenchmarkClientConnection;
import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.KillCurosorsMongodbPacket;
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
	final int nreturn  = Integer.parseInt(System.getProperty("return", "100"));
	private AtomicInteger index = new AtomicInteger();
	public MongodbBenchmarkClientConnection(SocketChannel channel, long createStamp,CountDownLatch latcher) {
		super(channel, createStamp,latcher);
	}

	public boolean needPing(long now) {
		return false;
	}

	public boolean checkIdle(long now) {
		return false;
	}

	@Override
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
		case MongodbPacketConstant.OP_REPLY:
			packet = new ResponseMongodbPacket();
			break;
		}
		packet.init(message, this);
		return packet;
	}

	@Override
	public AbstractMongodbPacket createRequestPacket() {
		QueryMongodbPacket packet = new QueryMongodbPacket();
		packet.fullCollectionName = "test.test";
		packet.numberToReturn = nreturn;
		packet.returnFieldSelector = new BasicBSONObject();
		packet.returnFieldSelector.put("f", 1);
		packet.numberToSkip = 0;
		packet.requestID = index.getAndIncrement();
		BasicBSONObject query = new BasicBSONObject();
		query.put("s", random.nextInt(400));
		packet.query = query;
		return packet;
	}

    protected void messageProcess() {
		//_handler.handleMessage(this);
    }
    
	public void postMessage(byte[] msg) {
		postMessage(ByteBuffer.wrap(msg));
	}
	
	@Override
	protected PacketInputStream createPacketInputStream() {
		return new MongodbFramedInputStream(true);
	}

	@Override
	protected PacketOutputStream createPacketOutputStream() {
		return new MongodbFramingOutputStream(true);
	}
}
