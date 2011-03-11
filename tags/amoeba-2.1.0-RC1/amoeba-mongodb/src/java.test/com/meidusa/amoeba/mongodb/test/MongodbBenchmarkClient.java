package com.meidusa.amoeba.mongodb.test;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.bson.BasicBSONObject;

import com.meidusa.amoeba.benchmark.AbstractBenchmarkClient;
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
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.net.packet.AbstractPacket;
import com.meidusa.amoeba.util.StringUtil;


/**
 * 
 * @author Struct
 *
 */
public class MongodbBenchmarkClient extends AbstractBenchmarkClient<AbstractMongodbPacket> {
	private static Logger	logger        = Logger.getLogger(MongodbBenchmarkClient.class);
	private boolean isLastModifyOperation = false;
	public MongodbBenchmarkClient(Connection conn, CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task) {
		super(conn,requestLatcher,responseLatcher,task);
	}

	public boolean needPing(long now) {
		return false;
	}

	public AbstractMongodbPacket decodeRecievedPacket(byte[] message) {
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
		default:
			logger.error("error type="+type+"\r\n"+StringUtil.dumpAsHex(message, message.length));
		}
		packet.init(message, this.getConnection());
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
		ParameterMapping.mappingObjectField(packet, properties,this.getNextRequestContextMap(),this, AbstractPacket.class);
		return packet;
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
		
		if(isLastModifyOperation){
			byte[] packetMessage = packet.toByteBuffer(this.getConnection()).array();
			byte[] lastError = getLastErrorPacket().toByteBuffer(this.getConnection()).array();
			byte[] message = new byte[packetMessage.length+lastError.length];
			System.arraycopy(packetMessage, 0, message, 0, packetMessage.length);
			System.arraycopy(lastError, 0, message, packetMessage.length,lastError.length);
			getConnection().postMessage(message);
		}else{
			getConnection().postMessage(packet.toByteBuffer(this.getConnection()));
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
	
	protected void postPacketToServer(){
		if(isLastModifyOperation){
			byte[] packetMessage = createRequestPacket().toByteBuffer(this.getConnection()).array();
			byte[] lastError = getLastErrorPacket().toByteBuffer(this.getConnection()).array();
			byte[] message = new byte[packetMessage.length+lastError.length];
			System.arraycopy(packetMessage, 0, message, 0, packetMessage.length);
			System.arraycopy(lastError, 0, message, packetMessage.length,lastError.length);
			getConnection().postMessage(message);
		}else{
			getConnection().postMessage(createRequestPacket().toByteBuffer(this.getConnection()));
		}
	}
}
