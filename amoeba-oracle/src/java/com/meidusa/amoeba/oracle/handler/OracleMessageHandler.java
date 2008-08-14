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
import com.meidusa.amoeba.oracle.packet.Packet;
import com.meidusa.amoeba.oracle.packet.ResendPacket;
import com.meidusa.amoeba.oracle.packet.SQLnetDef;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleMessageHandler implements MessageHandler, Sessionable, SQLnetDef {

    private Connection     clientConn;
    private Connection     serverConn;
    private MessageHandler clientHandler;
    private MessageHandler serverHandler;
    private boolean        isEnded        = false;

    private static int     msgCountLimit  = 8;
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
            if (clientMsgCount <= msgCountLimit) {
                clientMsgCount++;
            }

            switch (message[4]) {
                case NS_PACKT_TYPE_CONNECT:
                    message[32] = (byte) NSINADISABLEFORCONNECTION;
                    message[33] = (byte) NSINADISABLEFORCONNECTION;
                    break;
                case NS_PACKT_TYPE_DATA:
                    if (clientMsgCount == 3) {
                        AnoPacketBuffer buffer = new AnoPacketBuffer(message);
                        buffer.setPosition(10);
                        if (buffer.readUB4() == AnoServices.NA_MAGIC) {
                            AnoClientDataPacket packet = new AnoClientDataPacket();
                            packet.anoServiceSize = 0;
                            try {
                                clientConn.postMessage(packet.toBuffer().toByteBuffer().array());
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                    break;
            }

            // parseClientPacket(clientMsgCount, message);
            serverConn.postMessage(message);// proxy-->server
        } else {
            if (serverMsgCount <= msgCountLimit) {
                serverMsgCount++;
            }

            // parseServerPacket(serverMsgCount, message);
            clientConn.postMessage(message);// proxy-->client
        }
    }

    /**
     * 解析服务器端返回的数据包
     */
    @SuppressWarnings("unused")
    private void parseServerPacket(int count, byte[] msg) {
        Packet packet = null;
        switch (count) {
            case 1:
                if (msg[4] == NS_PACKT_TYPE_RESEND) {
                    packet = new ResendPacket();
                    break;
                } else {
                    throw new RuntimeException("Error data packet.");
                }
            case 2:
                if (msg[4] == NS_PACKT_TYPE_ACCEPT) {
                    packet = new AcceptPacket();
                    break;
                } else {
                    throw new RuntimeException("Error data packet.");
                }
            case 3:
                if (msg[4] == NS_PACKT_TYPE_DATA) {
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
    @SuppressWarnings("unused")
    private void parseClientPacket(int count, byte[] msg) {
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
