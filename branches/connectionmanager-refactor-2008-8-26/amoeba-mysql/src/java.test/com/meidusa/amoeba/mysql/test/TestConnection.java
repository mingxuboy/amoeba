package com.meidusa.amoeba.mysql.test;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.mysql.io.MysqlFramedInputStream;
import com.meidusa.amoeba.mysql.io.MysqlFramingOutputStream;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.util.StringUtil;

public class TestConnection extends Connection implements MessageHandler {
	private static Logger logger = Logger.getLogger(TestConnection.class);
	private int packetSequence;
	private PostMessageThread postRunner;
	static class PostMessageThread extends Thread{
		int i = 0;
		private Connection conn;
		public PostMessageThread(Connection conn){
			this.conn = conn;
			this.setDaemon(true);
		}
		public void run(){
			while(true){
				TestPacket packet = new TestPacket();
				packet.packetId = (byte)(i & 0xff);
				i++;
				packet.data = StringUtil.getRandomString(64);
				conn.postMessage(packet.toByteBuffer(null).array());
				if(i % 100==0)
				System.out.println("send:"+i);
			}
		}
	}
	public TestConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
		this.setMessageHandler(this);
		
	}
	
	public void init(){
		postRunner = new PostMessageThread(this);
		postRunner.start();
	}

	/**
	 * 为了提升性能，由于mysql数据包写到目的地的时候已经包含了包头，则不需要经过PacketOutputStream处理
	 */
	public void postMessage(byte[] msg)
    {
        ByteBuffer out= ByteBuffer.allocate(msg.length);
        out.put(msg);
        out.flip();
        if(_outQueue.size()>10000){
        	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
        }
        _outQueue.offer(out);
        _cmgr.notifyMessagePosted(this);
    }
	
	@Override
	protected PacketInputStream createPacketInputStream() {
		return new MysqlFramedInputStream(true);
	}
	
	@Override
	protected PacketOutputStream createPakcetOutputStream() {
		return new MysqlFramingOutputStream(true);
	}

	public void handleMessage(Connection conn, byte[] message) {
		TestPacket packet = new TestPacket();
		packet.init(message,conn);
		int sequence = packet.packetId & 0xff;
		if(sequence !=  (packetSequence & 0xff)){
			logger.error("packet sequence error,packet Sequence="+sequence +",but connectionSequeue="+packetSequence);
		}
		packetSequence++;
		if(packetSequence % 10000 == 0){
			System.out.println("recieved:"+packetSequence);
		}
	}
	
	public void postClose(Exception exception){
		super.postClose(exception);
		this.postRunner.stop();
	}

}
