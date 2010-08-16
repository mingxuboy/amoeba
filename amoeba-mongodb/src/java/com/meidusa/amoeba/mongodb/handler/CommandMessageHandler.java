package com.meidusa.amoeba.mongodb.handler;

import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;

public class CommandMessageHandler implements MessageHandler {

	public MessageHandler messageHandler;
	public Connection clientConn;
	public Connection[] serverConns;
	
	public CommandMessageHandler(Connection clientConn,Connection ...serverConns){
		this.clientConn = clientConn;
		this.serverConns = serverConns;
		for(Connection serverConn:serverConns){
			serverConn.setMessageHandler(this);
		}
	}
	
	@Override
	public void handleMessage(Connection conn) {
		try {
			byte[] message = null;
			if(conn == clientConn){
				while((message = conn.getInQueue().getNonBlocking()) != null){
					for(Connection serverConn:serverConns){
						AbstractMongodbPacket packet = new AbstractMongodbPacket();
						packet.init(message, conn);
						serverConn.postMessage(message);
					}
				}
			}else{
				while((message = conn.getInQueue().getNonBlocking()) != null){
					AbstractMongodbPacket packet = new AbstractMongodbPacket();
					packet.init(message, conn);
					clientConn.postMessage(message);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
