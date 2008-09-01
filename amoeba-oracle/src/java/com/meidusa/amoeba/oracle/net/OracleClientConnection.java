package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.Packet;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.oracle.context.OracleProxyRuntimeContext;
import com.meidusa.amoeba.oracle.handler.OracleQueryMessageHandler;
import com.meidusa.amoeba.oracle.net.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.net.packet.AnoDataPacket;
import com.meidusa.amoeba.oracle.net.packet.AnoResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIMsgPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoer;
import com.meidusa.amoeba.util.StringUtil;

public class OracleClientConnection extends OracleConnection implements SQLnetDef {

    private static Logger logger          = Logger.getLogger(OracleClientConnection.class);
    private String        defaultPoolName = null;
    private ObjectPool    pool            = null;
    private String        encryptedSK;

    public OracleClientConnection(SocketChannel channel, long createStamp){
        super(channel, createStamp);
        defaultPoolName = OracleProxyRuntimeContext.getInstance().getQueryRouter().getDefaultPool();
        pool = OracleProxyRuntimeContext.getInstance().getPoolMap().get(defaultPoolName);
    }

    public void handleMessage(Connection conn, byte[] message) {
        OracleClientConnection clientConn = (OracleClientConnection) conn;
        Packet packet = null;
        Packet response = null;
        switch (message[4]) {
            case NS_PACKT_TYPE_CONNECT:
                ConnectPacket connPacket = new ConnectPacket();
                packet = connPacket;
                packet.init(message, conn);
                clientConn.setAnoEnabled(connPacket.anoEnabled);
                response = new AcceptPacket();
                break;
            case NS_PACKT_TYPE_DATA:
                if (clientConn.isAnoEnabled() && AnoDataPacket.isAnoType(message)) {
                    response = new AnoResponseDataPacket();
                } else if (T4CTTIMsgPacket.isMsgType(message, T4CTTIMsgPacket.TTIPRO)) {
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
                    this.encryptedSK = StringUtil.getRandomString(16);
                    ((T4CTTIoAuthKeyResponseDataPacket) response).encryptedSK = this.encryptedSK;
                } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OAUTH)) {
                    T4CTTIoAuthDataPacket aPacket = new T4CTTIoAuthDataPacket();
                    packet = aPacket;
                    // ((T4CTTIoAuthDataPacket) packet).encryptedSK = this.encryptedSK;
                    packet.init(message, conn);

                    String encryptedPassword = T4CTTIoAuthDataPacket.encryptPassword(this.getUser(),
                                                                                     this.getPassword(),
                                                                                     this.encryptedSK.getBytes(),
                                                                                     this.getConversion());

                    T4CTTIoAuthResponseDataPacket responsePacket = new T4CTTIoAuthResponseDataPacket();
                    response = responsePacket;
                    responsePacket.oer = new T4CTTIoer(new T4CPacketBuffer(32));
                    if (!StringUtil.equals(encryptedPassword, aPacket.map.get("AUTH_PASSWORD"))) {
                        responsePacket.oer.retCode = 1017;
                        responsePacket.oer.errorMsg = "ORA-01017: invalid username/password; logon denied";
                        this.setAuthenticated(false);
                    }
                }

                break;
        }

        if (packet != null) {
            if (logger.isDebugEnabled()) {
                System.out.println("========================================================");
                System.out.println("packet:" + packet);
                // System.out.println("###source:" + ByteUtil.toHex(message, 0, message.length));
            }
        }

        if (response instanceof T4C8TTIproResponseDataPacket) {
            OracleConnection.setProtocolField((OracleConnection) conn, (T4C8TTIproResponseDataPacket) response);
        } else if (response instanceof T4C8TTIdtyResponseDataPacket) {
            clientConn.setBasicTypes();
        } else if (response instanceof T4CTTIoAuthResponseDataPacket) {
            switchHandler();
        }

        byte[] responseMessage = response.toByteBuffer(conn).array();
        if (logger.isDebugEnabled()) {
            System.out.println("responsePacket:" + response);
            // System.out.println("#response:" + ByteUtil.toHex(responseMessage, 0, responseMessage.length));
            System.out.println();
        }

        postMessage(responseMessage);
    }

    private void switchHandler() {
        try {
            Connection dst = (Connection) pool.borrowObject();
            new OracleQueryMessageHandler(this, dst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
