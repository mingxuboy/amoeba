package com.meidusa.amoeba.oracle.handler;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.net.packet.AnoDataPacket;
import com.meidusa.amoeba.oracle.net.packet.AnoPacketBuffer;
import com.meidusa.amoeba.oracle.net.packet.AnoServices;
import com.meidusa.amoeba.oracle.net.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.net.packet.Packet;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyResponseDataPacket;
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
    private Packet         lastPackt      = null;
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
            Packet packet = null;
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
                            clientConn.postMessage(packet.toByteBuffer(conn).array());
                            return;
                        }
                    }
                    if (clientMsgCount <= 8) {
                        if (T4CTTIfunPacket.isMsgType(message, T4CTTIfunPacket.TTIPRO)) {
                            packet = new T4C8TTIproDataPacket();
                        } else if (T4CTTIfunPacket.isMsgType(message, T4CTTIfunPacket.TTIDTY)) {
                            packet = new T4C8TTIdtyDataPacket();
                        } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OVERSION)) {
                            packet = new T4C7OversionDataPacket();
                        } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OSESSKEY)) {
                            packet = new T4CTTIoAuthKeyDataPacket();
                        }
                    }
                    break;
            }

            if (packet != null) {
                packet.init(message, conn);
                if (logger.isDebugEnabled()) {
                    System.out.println("========================================================");
                    System.out.println("packet:" + packet);
                    System.out.println("##source:" + ByteUtil.toHex(message, 0, message.length));
                }
                message = packet.toByteBuffer(conn).array();
                if (logger.isDebugEnabled()) {
                    System.out.println("#warpped:" + ByteUtil.toHex(message, 0, message.length));
                    System.out.println();
                }
                lastPackt = packet;
            }
            serverConn.postMessage(message);// proxy-->server
        } else {
            serverMsgCount++;
            Packet packet = null;
            switch (message[4]) {
                case NS_PACKT_TYPE_DATA:
                    if (lastPackt instanceof T4C8TTIproDataPacket) {
                        packet = new T4C8TTIproResponseDataPacket();
                    } else if (lastPackt instanceof T4C8TTIdtyDataPacket) {
                        packet = new T4C8TTIdtyResponseDataPacket();
                    } else if (lastPackt instanceof T4C7OversionDataPacket) {
                        packet = new T4C7OversionResponseDataPacket();
                    } else if (lastPackt instanceof T4CTTIoAuthKeyDataPacket) {
                        packet = new T4CTTIoAuthKeyResponseDataPacket();
                    }
                    break;
            }

            try {
                if (packet != null) {
                    packet.init(message, conn);
                    if (logger.isDebugEnabled()) {
                        System.out.println("reponse packet:" + packet);
                        System.out.println("@@server source:" + ByteUtil.toHex(message, 0, message.length));
                    }
                    message = packet.toByteBuffer(conn).array();
                    if (logger.isDebugEnabled()) {
                        System.out.println("@server warpped:" + ByteUtil.toHex(message, 0, message.length));
                        System.out.println();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            lastPackt = null;
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
