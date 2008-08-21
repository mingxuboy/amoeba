package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.oracle.context.OracleProxyRuntimeContext;
import com.meidusa.amoeba.oracle.handler.OracleMessageHandler;
import com.meidusa.amoeba.oracle.net.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.net.packet.AnoResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.net.packet.Packet;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIMsgPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyResponseDataPacket;
import com.meidusa.amoeba.oracle.util.ByteUtil;

public class OracleClientConnection extends OracleConnection implements SQLnetDef {

    private static Logger logger          = Logger.getLogger(OracleClientConnection.class);
    private String        defaultPoolName = null;
    private ObjectPool    pool            = null;
    private int           msgCount        = 0;
    private int           clientMsgCount  = 0;
    private byte[]        encryptedSK;
    private Packet        lastPackt       = null;

    public OracleClientConnection(SocketChannel channel, long createStamp){
        super(channel, createStamp);
        defaultPoolName = OracleProxyRuntimeContext.getInstance().getQueryRouter().getDefaultPool();
        pool = OracleProxyRuntimeContext.getInstance().getPoolMap().get(defaultPoolName);
        switchHandler();
    }

    public void handleMessage(Connection conn, byte[] message) {
        OracleClientConnection clientConn = (OracleClientConnection) conn;
        clientMsgCount++;
        Packet packet = null;
        Packet response = null;
        switch (message[4]) {
            case NS_PACKT_TYPE_CONNECT:
                message[32] = (byte) NSINADISABLEFORCONNECTION;
                message[33] = (byte) NSINADISABLEFORCONNECTION;
                packet = new ConnectPacket();
                packet.init(message, conn);
                response = new AcceptPacket();
                break;
            case NS_PACKT_TYPE_DATA:
                if (clientMsgCount <= 9) {
                    if (T4CTTIMsgPacket.isMsgType(message, T4CTTIMsgPacket.TTIPRO)) {
                        packet = new T4C8TTIproDataPacket();
                        packet.init(message, conn);
                        response = new T4C8TTIproResponseDataPacket();

                    } else if (T4CTTIMsgPacket.isMsgType(message, T4CTTIMsgPacket.TTIDTY)) {
                        packet = new T4C8TTIdtyDataPacket();
                        packet.init(message, conn);
                        response = new T4C8TTIdtyResponseDataPacket();

                    } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OVERSION)) {
                        packet = new T4C7OversionDataPacket();
                        packet.init(message, conn);
                        response = new T4C7OversionResponseDataPacket();

                    } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OSESSKEY)) {
                        packet = new T4CTTIoAuthKeyDataPacket();
                        packet.init(message, conn);
                        response = new T4CTTIoAuthKeyResponseDataPacket();

                    } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OAUTH)) {
                        packet = new T4CTTIoAuthDataPacket();
                        ((T4CTTIoAuthDataPacket) packet).encryptedSK = this.encryptedSK;
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

            // if(!(packet instanceof T4CTTIoAuthDataPacket)){
            message = packet.toByteBuffer(conn).array();
            // }
            if (logger.isDebugEnabled()) {
                System.out.println("#warpped:" + ByteUtil.toHex(message, 0, message.length));
                System.out.println();
            }
            lastPackt = packet;
        }

        msgCount++;

        if (msgCount == 1) {
            if (message[4] == Packet.NS_PACKT_TYPE_CONNECT) {
                ConnectPacket connPacket = new ConnectPacket();
                connPacket.init(message, conn);
                clientConn.setAnoEnabled(connPacket.anoEnabled);
                packet = new AcceptPacket();
            } else {
                throw new RuntimeException("Error data packet.");
            }
        }
        if (msgCount == 2) {
            if (clientConn.isAnoEnabled()) {
                packet = new AnoResponseDataPacket();
            } else {

            }
        }
        // ...

        postMessage(response.toByteBuffer(conn).array());
    }

    private void switchHandler() {
        try {
            Connection dst = (Connection) pool.borrowObject();
            new OracleMessageHandler(this, dst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
