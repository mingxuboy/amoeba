package com.meidusa.amoeba.oracle.handler;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.packet.AnoDataPacket;
import com.meidusa.amoeba.oracle.packet.AnoPacketBuffer;
import com.meidusa.amoeba.oracle.packet.AnoServices;
import com.meidusa.amoeba.oracle.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.packet.Packet;
import com.meidusa.amoeba.oracle.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.packet.T4C7OversionDataPacket;
import com.meidusa.amoeba.oracle.packet.T4C8TTIdtyDataPacket;
import com.meidusa.amoeba.oracle.packet.T4C8TTIproDataPacket;
import com.meidusa.amoeba.oracle.packet.T4C8TTIproResponseDataPacket;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleMessageHandler implements MessageHandler, Sessionable, SQLnetDef {

    private static Logger  logger         = Logger.getLogger(OracleMessageHandler.class);

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
            Packet packet = null;
            clientMsgCount++;

            switch (message[4]) {
                case NS_PACKT_TYPE_CONNECT:
                    message[32] = (byte) NSINADISABLEFORCONNECTION;
                    message[33] = (byte) NSINADISABLEFORCONNECTION;
                    packet = new ConnectPacket();
                    break;
                case NS_PACKT_TYPE_DATA:
                    if (clientMsgCount == 3) {
                        AnoPacketBuffer buffer = new AnoPacketBuffer(message);
                        buffer.setPosition(10);
                        if (buffer.readUB4() == AnoServices.NA_MAGIC) {
                            packet = new AnoDataPacket();
                            ((AnoDataPacket) packet).anoServiceSize = 0;
                            serverMsgCount++;
                            clientConn.postMessage(packet.toByteBuffer().array());
                            return;
                        }
                    }

                    if (clientMsgCount == 4) {
                        packet = new T4C8TTIproDataPacket();
                    }

                    if (clientMsgCount == 5) {
                        packet = new T4C8TTIdtyDataPacket();
                    }

                    if (clientMsgCount == 6) {
                        packet = new T4C7OversionDataPacket();
                    }

                    break;
            }

            if (packet != null) {
                packet.init(message);
                byte[] ab = packet.toByteBuffer().array();
                if (logger.isDebugEnabled()) {
                    System.out.println("#receive packet:" + packet);
                    System.out.println("#receive bytes:" + ByteUtil.toHex(ab, 0, ab.length));
                    System.out.println();
                }
                serverConn.postMessage(ab);
            } else {
                serverConn.postMessage(message);// proxy-->server
            }

        } else {
            serverMsgCount++;

            switch (message[4]) {
                case NS_PACKT_TYPE_DATA:
                    if (clientMsgCount == 4) {
                        Packet packet = new T4C8TTIproResponseDataPacket();
                        System.out.println("@send bytes:" + ByteUtil.toHex(message, 0, message.length));
                        packet.init(message);
                        message = packet.toByteBuffer().array();
                        if (logger.isDebugEnabled()) {
                            System.out.println("@send bytes:" + ByteUtil.toHex(message, 0, message.length));
                            System.out.println();
                        }
                    }
                    break;
            }

            clientConn.postMessage(message);// proxy-->client
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
