package com.meidusa.amoeba.oracle.handler;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.packet.AnoClientDataPacket;
import com.meidusa.amoeba.oracle.packet.AnoPacketBuffer;
import com.meidusa.amoeba.oracle.packet.AnoServerDataPacket;
import com.meidusa.amoeba.oracle.packet.AnoServices;
import com.meidusa.amoeba.oracle.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.packet.Packet;
import com.meidusa.amoeba.oracle.packet.ResendPacket;
import com.meidusa.amoeba.oracle.packet.SQLnetDef;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleMessageHandler implements MessageHandler, Sessionable,SQLnetDef {

    private Connection     clientConn;
    private Connection     serverConn;
    private MessageHandler clientHandler;
    private MessageHandler serverHandler;
    private boolean        isEnded        = false;

    private int            serverMsgCount = 0;
    private int            clientMsgCount = 0;

    public OracleMessageHandler(Connection clientConn, Connection serverConn){
        this.clientConn = clientConn;
        clientHandler = clientConn.getMessageHandler();
        this.serverConn = serverConn;
        serverHandler = serverConn.getMessageHandler();
        clientConn.setMessageHandler(this);
        serverConn.setMessageHandler(this);
    }

    public void handleMessage(Connection conn, byte[] message) {
        if (conn == clientConn) {
            clientMsgCount++;
            //parseClientPacket(clientMsgCount, message);
            if(message[4] == Packet.NS_PACKT_TYPE_CONNECT){
            	message[32] = (byte)(NSINADISABLEFORCONNECTION & 0xff);
            	message[33] = (byte)(NSINADISABLEFORCONNECTION & 0xff);
            }
            
            if(clientMsgCount ==3){
	            if (message[4] == Packet.NS_PACKT_TYPE_DATA) {
	            	AnoPacketBuffer buffer = new AnoPacketBuffer(message);
	            	buffer.setPosition(10);
	            	long magic = buffer.readUB4();
	            	if(magic == AnoServices.NA_MAGIC){
	            		AnoClientDataPacket dataPacket = new AnoClientDataPacket();
	            		dataPacket.anoServiceSize = 0;
	            		try {
							this.clientConn.postMessage(dataPacket.toBuffer().toByteBuffer().array());
							return;
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
	            	}
	            }
            }
            serverConn.postMessage(message);// proxy-->server
        } else {
            serverMsgCount++;
            //parseServerPacket(serverMsgCount, message);
            clientConn.postMessage(message);// proxy-->client
        }
    }

    /**
     * 解析服务器端返回的数据包
     */
    private void parseServerPacket(int count, byte[] msg) {
        Packet packet = null;
        switch (count) {
            case 1:
                if (msg[4] == Packet.NS_PACKT_TYPE_RESEND) {
                    packet = new ResendPacket();
                    break;
                } else {
                    throw new RuntimeException("Error data packet.");
                }
            case 2:
                if (msg[4] == Packet.NS_PACKT_TYPE_ACCEPT) {
                    packet = new AcceptPacket();
                    break;
                } else {
                    throw new RuntimeException("Error data packet.");
                }
            case 3:
                if (msg[4] == Packet.NS_PACKT_TYPE_DATA) {
                    packet = new AnoServerDataPacket();
                    break;
                } else {
                    throw new RuntimeException("Error data packet.");
                }
        }
        if (packet != null) {
            packet.init(msg);
        }
    }

    /**
     *解析客户端发送的数据包
     */
    private void parseClientPacket(int count, byte[] msg) {
        Packet packet = null;
        switch (count) {
            case 1:
                if (msg[4] == Packet.NS_PACKT_TYPE_CONNECT) {
                    packet = new ConnectPacket();
                    break;
                } else {
                    throw new RuntimeException("Error data packet.");
                }
            case 2:
                if (msg[4] == Packet.NS_PACKT_TYPE_CONNECT) {
                    packet = new ConnectPacket();
                    break;
                } else {
                    throw new RuntimeException("Error data packet.");
                }
            case 3:
                break;
        }
        if (packet != null) {
            packet.init(msg);
        }
    }

    public boolean checkIdle(long now) {
        return false;
    }

    public synchronized void endSession() {
        if (!isEnded()) {
            isEnded = true;
            clientConn.setMessageHandler(clientHandler);
            serverConn.setMessageHandler(serverHandler);
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
