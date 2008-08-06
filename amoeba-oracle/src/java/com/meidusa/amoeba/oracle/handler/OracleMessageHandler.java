package com.meidusa.amoeba.oracle.handler;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
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
		if(conn == clientConn){
			serverConn.postMessage(message);
			
			/**
			 * 从客户端发送过来的验证信息包
			 */
			if(message[4] == (byte)Packet.NS_PACKT_TYPE_CONNECT){
				ConnectPacket packet = new ConnectPacket();
				packet.init(message);
			}
			
		}else{
			clientConn.postMessage(message);
		}
	}
	public boolean checkIdle(long now) {
		return false;
	}
	public void endSession() {
		clientConn.postClose(null);
		serverConn.postClose(null);
		isEnded = true;
	}
	
	public boolean isEnded() {
		return isEnded;
	}
	public void startSession() throws Exception {
		
	}

}
