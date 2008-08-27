package com.meidusa.amoeba.mysql.test;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.mysql.net.packet.BlockedPacketIO;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.util.StringUtil;

public class Client {
	
	private static Logger logger = Logger.getLogger(Client.class);
	public static void main(String[] args) throws Exception{
		final Socket socket = new Socket(System.getProperty("ip", "127.0.0.1"),10004);
		new Thread(){
			public void run(){
				int i = 0;
				while(true){
					TestPacket packet = new TestPacket();
					packet.packetId = (byte)(i++ & 0xff);
					packet.data = StringUtil.getRandomString(64);
					try {
						socket.getOutputStream().write(packet.toByteBuffer(null).array());
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		};//.start();
		
		new Thread(){
			public void run(){
				int packetSequence = 0;
				while(true){
					TestPacket packet = new TestPacket();
					try {
						MysqlPacketBuffer buffer = BlockedPacketIO.readFullyPacketBuffer(socket.getInputStream(), MysqlPacketBuffer.class);
						packet.init(buffer);
						int sequence = packet.packetId & 0xff;
						packetSequence = packetSequence & 0xff;
						if(sequence !=  packetSequence){
							logger.error("packet sequence error,packet Sequence="+sequence +",but connectionSequeue="+packetSequence);
						}
						System.out.println(sequence);
						packetSequence++;
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		}.start();
	}
}
