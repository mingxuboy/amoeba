package com.meidusa.amoeba.oracle.handler;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.packet.AnoClientDataPacket;
import com.meidusa.amoeba.oracle.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.packet.Packet;
import com.meidusa.amoeba.oracle.packet.ResendPacket;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleMessageHandler implements MessageHandler, Sessionable {

    private Connection     clientConn;
    private Connection     serverConn;
    private MessageHandler clientHandler;
    private MessageHandler serverHandler;
    private boolean        isEnded        = false;

    private int            clientMsgCount = 0;
    private int            serverMsgCount = 0;

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
            serverMsgCount++;
            serverPacket(serverMsgCount, message);
            serverConn.postMessage(message);// proxy-->server
        } else {
            clientMsgCount++;
            clientPacket(clientMsgCount, message);
            clientConn.postMessage(message);// proxy-->client
        }
    }

    private void clientPacket(int count, byte[] msg) {
        Packet packet = null;
        switch (count) {
            case 1:
                if (msg[4] == Packet.NS_PACKT_TYPE_RESEND) {
                    packet = new ResendPacket();
                } else {
                    throw new RuntimeException("Error data packet.");
                }
                break;
        }
        if (packet != null) {
            packet.init(msg);
        }
    }

    private void serverPacket(int count, byte[] msg) {
        Packet packet = null;
        switch (count) {
            case 1:
                if (msg[4] == Packet.NS_PACKT_TYPE_CONNECT) {
                    packet = new ConnectPacket();
                } else {
                    throw new RuntimeException("Error data packet.");
                }
                break;
            case 2:
                if (msg[4] == Packet.NS_PACKT_TYPE_CONNECT) {
                    packet = new ConnectPacket();
                } else {
                    throw new RuntimeException("Error data packet.");
                }
                break;
            case 3:
                if (msg[4] == Packet.NS_PACKT_TYPE_DATA) {
                    packet = new AnoClientDataPacket();
                } else {
                    throw new RuntimeException("Error data packet.");
                }
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
