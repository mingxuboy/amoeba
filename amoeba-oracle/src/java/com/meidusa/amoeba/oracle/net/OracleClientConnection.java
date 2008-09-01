package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
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
import com.meidusa.amoeba.oracle.util.ByteUtil;
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
        byte[] respMessage = null;

        if (logger.isDebugEnabled()) {
            System.out.println("========================================================");
            System.out.println("####message:" + ByteUtil.toHex(message, 0, message.length));
        }
        switch (message[4]) {
            case NS_PACKT_TYPE_CONNECT:
                ConnectPacket connPacket = new ConnectPacket();
                connPacket.init(message, conn);

                clientConn.setAnoEnabled(connPacket.anoEnabled);
                AcceptPacket aptPacket = new AcceptPacket();
                respMessage = aptPacket.toByteBuffer(conn).array();
                break;

            case NS_PACKT_TYPE_DATA:
                if (clientConn.isAnoEnabled() && AnoDataPacket.isAnoType(message)) {
                    AnoResponseDataPacket anoRespPacket = new AnoResponseDataPacket();
                    respMessage = anoRespPacket.toByteBuffer(conn).array();

                } else if (T4CTTIMsgPacket.isMsgType(message, T4CTTIMsgPacket.TTIPRO)) {
                    T4C8TTIproDataPacket proPacket = new T4C8TTIproDataPacket();
                    proPacket.init(message, conn);

                    T4C8TTIproResponseDataPacket proRespPacket = new T4C8TTIproResponseDataPacket();
                    // OracleConnection.setProtocolField((OracleConnection) conn, proRespPacket);
                    respMessage = proRespPacket.toByteBuffer(conn).array();

                } else if (T4CTTIMsgPacket.isMsgType(message, T4CTTIMsgPacket.TTIDTY)) {
                    T4C8TTIdtyDataPacket dtyPacket = new T4C8TTIdtyDataPacket();
                    dtyPacket.init(message, conn);

                    T4C8TTIdtyResponseDataPacket dtyRespPacket = new T4C8TTIdtyResponseDataPacket();
                    clientConn.setBasicTypes();
                    respMessage = dtyRespPacket.toByteBuffer(conn).array();

                } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OVERSION)) {
                    T4C7OversionDataPacket versionPacket = new T4C7OversionDataPacket();
                    versionPacket.init(message, conn);

                    T4C7OversionResponseDataPacket versionRespPacket = new T4C7OversionResponseDataPacket();
                    respMessage = versionRespPacket.toByteBuffer(conn).array();

                } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OSESSKEY)) {
                    T4CTTIoAuthKeyDataPacket authKeyPacket = new T4CTTIoAuthKeyDataPacket();
                    authKeyPacket.init(message, conn);

                    T4CTTIoAuthKeyResponseDataPacket authKeyRespPacket = new T4CTTIoAuthKeyResponseDataPacket();
                    this.encryptedSK = StringUtil.getRandomString(16);
                    authKeyRespPacket.encryptedSK = this.encryptedSK;
                    respMessage = authKeyRespPacket.toByteBuffer(conn).array();

                } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OAUTH)) {
                    T4CTTIoAuthDataPacket authPacket = new T4CTTIoAuthDataPacket();
                    authPacket.init(message, conn);

                    String encryptedPassword = T4CTTIoAuthDataPacket.encryptPassword(this.getUser(), this.getPassword(), this.encryptedSK.getBytes(), this.getConversion());

                    T4CTTIoAuthResponseDataPacket authRespPacket = new T4CTTIoAuthResponseDataPacket();
                    authRespPacket.oer = new T4CTTIoer(new T4CPacketBuffer(32));
                    if (!StringUtil.equals(encryptedPassword, authPacket.map.get(T4CTTIoAuthResponseDataPacket.AUTH_PASSWORD))) {
                        authRespPacket.oer.retCode = 1017;
                        authRespPacket.oer.errorMsg = "ORA-01017: invalid username/password; logon denied";
                        this.setAuthenticated(false);
                    }
                    respMessage = authRespPacket.toByteBuffer(conn).array();
                    switchHandler();
                }
                break;
        }
        if (logger.isDebugEnabled()) {
            System.out.println("respMessage:" + ByteUtil.toHex(respMessage, 0, respMessage.length));
            System.out.println();
        }

        postMessage(respMessage);
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
