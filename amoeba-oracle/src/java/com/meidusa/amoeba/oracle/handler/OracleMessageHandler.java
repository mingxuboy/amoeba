package com.meidusa.amoeba.oracle.handler;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;

/**
 * 非常简单的数据包转发程序
 * @author struct
 *
 */
public class OracleMessageHandler implements MessageHandler {

	private Connection clientConn;
	private Connection serverConn;
	public OracleMessageHandler(Connection clientConn,Connection serverConn){
		this.clientConn = clientConn;
		this.serverConn = serverConn;
	}
	public void handleMessage(Connection conn, byte[] message) {
		if(conn == clientConn){
			serverConn.postMessage(message);
		}else{
			clientConn.postMessage(message);
		}
	}

}
