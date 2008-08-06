package com.meidusa.amoeba.oracle.handler;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.packet.Packet;

/**
 * 非常简单的数据包转发程序
 * @author struct
 *
 */
public class OracleMessageHandler implements MessageHandler,Sessionable {

	private Connection clientConn;
	private Connection serverConn;
	private boolean isEnded = false;
	public OracleMessageHandler(Connection clientConn,Connection serverConn){
		this.clientConn = clientConn;
		this.serverConn = serverConn;
	}
	public void handleMessage(Connection conn, byte[] message) {
		Packet packet = null;
		if(conn == clientConn){
			serverConn.postMessage(message);
			
			/**
			 * 从客户端发送过来的验证信息包
			 */
			if(message[4] == (byte)Packet.NS_PACKT_TYPE_CONNECT){
				packet = new ConnectPacket();
			}
			
		}else{
			
			/**
			 * 从服务端发送过来的接收连接的数据包
			 */
			if(message[4] == (byte)Packet.NS_PACKT_TYPE_ACCEPT){
				packet = new AcceptPacket();
			}
			clientConn.postMessage(message);
		}
		
		if(packet != null){
			packet.init(message);
		}
	}
	public boolean checkIdle(long now) {
		return false;
	}
	
	public synchronized void endSession() {
		if(!isEnded()){
			isEnded = true;
			clientConn.postClose(null);
			serverConn.postClose(null);
		}
	}
	
	public boolean isEnded() {
		return isEnded;
	}
	public void startSession() throws Exception {
		
	}

}
