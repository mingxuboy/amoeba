package com.meidusa.amoeba.oracle.handler;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.packet.Packet;
import com.meidusa.amoeba.oracle.packet.ResendPacket;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleMessageHandler implements MessageHandler, Sessionable {

    private Connection clientConn;
    private Connection serverConn;
    private boolean    isEnded = false;

    public OracleMessageHandler(Connection clientConn, Connection serverConn){
        this.clientConn = clientConn;
        this.serverConn = serverConn;
    }

    public void handleMessage(Connection conn, byte[] message) {
        Packet packet = null;
        if (conn == clientConn) {
            serverConn.postMessage(message);

            // client-->proxy-->server
            packet = this.getPacket(message[4]);
        } else {
            // server-->proxy-->client
            packet = this.getPacket(message[4]);

            clientConn.postMessage(message);
        }

        if (packet != null) {
            packet.init(message);
        }
    }

    public boolean checkIdle(long now) {
        return false;
    }

    public synchronized void endSession() {
        if (!isEnded()) {
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

    private Packet getPacket(int type) {
        Packet packet = null;
        switch (type) {
            case Packet.NS_PACKT_TYPE_CONNECT:
                packet = new ConnectPacket();
                break;
            case Packet.NS_PACKT_TYPE_ACCEPT:
                packet = new AcceptPacket();
                break;
            case Packet.NS_PACKT_TYPE_ACK:
                break;
            case Packet.NS_PACKT_TYPE_REFUTE:
                break;
            case Packet.NS_PACKT_TYPE_REDIRECT:
                break;
            case Packet.NS_PACKT_TYPE_DATA:
                break;
            case Packet.NS_PACKT_TYPE_NULL:
                break;
            case Packet.NS_PACKT_TYPE_ABORT:
                break;
            case Packet.NS_PACKT_TYPE_RESEND:
                packet = new ResendPacket();
                break;
            case Packet.NS_PACKT_TYPE_MARKER:
                break;
            case Packet.NS_PACKT_TYPE_ATTENTION:
                break;
            case Packet.NS_PACKT_TYPE_CONTROL:
                break;
            case Packet.NS_PACKT_TYPE_HI:
                break;
            default:
                throw new RuntimeException("unknowing packet type:" + type);
        }
        return packet;
    }

}
