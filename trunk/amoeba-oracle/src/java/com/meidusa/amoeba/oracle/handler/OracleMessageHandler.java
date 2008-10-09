package com.meidusa.amoeba.oracle.handler;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.packet.Packet;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.net.packet.AnoDataPacket;
import com.meidusa.amoeba.oracle.net.packet.AnoPacketBuffer;
import com.meidusa.amoeba.oracle.net.packet.AnoServices;
import com.meidusa.amoeba.oracle.net.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8OcloseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIMsgPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthResponseDataPacket;
import com.meidusa.amoeba.oracle.util.ByteUtil;
import com.meidusa.amoeba.oracle.util.DBConversion;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleMessageHandler implements MessageHandler, Sessionable, SQLnetDef {

    private static Logger    logger         = Logger.getLogger(OracleMessageHandler.class);

    private OracleConnection clientConn;
    private OracleConnection serverConn;
    private MessageHandler   clientHandler;
    private MessageHandler   serverHandler;
    private boolean          isEnded        = false;
    private Packet           lastPackt      = null;
    private int              serverMsgCount = 0;
    private int              clientMsgCount = 0;
    private String           encryptedSK;

    public OracleMessageHandler(Connection clientConn, Connection serverConn){
        this.clientConn = (OracleConnection) clientConn;
        clientHandler = clientConn.getMessageHandler();
        this.serverConn = (OracleConnection) serverConn;
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

                    if (T4CTTIMsgPacket.isMsgType(message, T4CTTIMsgPacket.TTIPRO)) {
                        packet = new T4C8TTIproDataPacket();
                    } else if (T4CTTIMsgPacket.isMsgType(message, T4CTTIMsgPacket.TTIDTY)) {
                        packet = new T4C8TTIdtyDataPacket();
                    } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OVERSION)) {
                        packet = new T4C7OversionDataPacket();
                    } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OSESSKEY)) {
                        packet = new T4CTTIoAuthKeyDataPacket();
                    } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OAUTH)) {
                        packet = new T4CTTIoAuthDataPacket();
                        ((T4CTTIoAuthDataPacket) packet).encryptedSK = this.encryptedSK;
                    } else if (T4CTTIfunPacket.isFunType(message, T4CTTIMsgPacket.TTIPFN, T4CTTIfunPacket.OCCA)) {
                        Packet packet1 = new T4C8OcloseDataPacket();
                        packet1.init(message, conn);
                        if (logger.isDebugEnabled()) {
                            logger.debug("packet1:" + packet1);
                        }
                    }

                    break;
            }

            if (packet != null) {
                packet.init(message, conn);
                if (logger.isDebugEnabled()) {
                    logger.debug("========================================================");
                    logger.debug("packet:" + packet);
                    logger.debug("##source:" + ByteUtil.toHex(message, 0, message.length));
                }

                // if(!(packet instanceof T4CTTIoAuthDataPacket)){
                message = packet.toByteBuffer(conn).array();
                // }
                if (logger.isDebugEnabled()) {
                    logger.debug("#warpped:" + ByteUtil.toHex(message, 0, message.length));
                    logger.debug("");
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
                    } else if (lastPackt instanceof T4CTTIoAuthDataPacket) {
                        packet = new T4CTTIoAuthResponseDataPacket();
                    }
                    break;
            }

            try {
                if (packet != null) {
                    packet.init(message, conn);

                    if (packet instanceof T4C8TTIproResponseDataPacket) {
                        setConnectionField(clientConn, (T4C8TTIproResponseDataPacket) packet);
                        setConnectionField(serverConn, (T4C8TTIproResponseDataPacket) packet);
                    } else if (packet instanceof T4CTTIoAuthKeyResponseDataPacket) {
                        this.encryptedSK = ((T4CTTIoAuthKeyResponseDataPacket) packet).encryptedSK;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("reponse packet:" + packet);
                        logger.debug("@@server source:" + ByteUtil.toHex(message, 0, message.length));
                    }
                    message = packet.toByteBuffer(conn).array();
                    
                    if(packet instanceof T4C8TTIdtyResponseDataPacket){
                    	clientConn.setBasicTypes();
                    	serverConn.setBasicTypes();
                    }
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("@server warpped:" + ByteUtil.toHex(message, 0, message.length));
                        logger.debug("");
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

    public void setConnectionField(OracleConnection conn, T4C8TTIproResponseDataPacket packet) {
        T4C8TTIproResponseDataPacket pro = (T4C8TTIproResponseDataPacket) packet;
        short word0 = pro.oVersion;
        short word1 = pro.svrCharSet;
        short word2 = DBConversion.findDriverCharSet(word1, word0);

        try {
            DBConversion conversion = new DBConversion(word1, word2, pro.NCHAR_CHARSET);
            conn.setConversion(conversion);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        conn.getRep().setServerConversion(word2 != word1);
        conn.getRep().setVersion(word0);
        if (DBConversion.isCharSetMultibyte(word2)) {
            if (DBConversion.isCharSetMultibyte(pro.svrCharSet)) conn.getRep().setFlags((byte) 1);
            else conn.getRep().setFlags((byte) 2);
        } else {
            conn.getRep().setFlags(pro.svrFlags);
        }
    }

    public boolean isEnded() {
        return isEnded;
    }

    public void startSession() throws Exception {
    }

}
