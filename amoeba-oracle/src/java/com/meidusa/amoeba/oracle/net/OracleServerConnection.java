package com.meidusa.amoeba.oracle.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.Packet;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.oracle.net.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.net.packet.RedirectPacket;
import com.meidusa.amoeba.oracle.net.packet.RefusePacket;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIMsgPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthResponseDataPacket;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * @author struct
 */
public class OracleServerConnection extends OracleConnection implements PoolableObject, SQLnetDef {

    private static Logger logger = Logger.getLogger(OracleServerConnection.class);
    private boolean       active;
    private ObjectPool    objectPool;
    private Packet        lastPacketRequest;

    public OracleServerConnection(SocketChannel channel, long createStamp){
        super(channel, createStamp);
    }

    @Override
    protected void init() {
        ConnectPacket packet = genConnectPacket();
        ByteBuffer byteBuffer = packet.toByteBuffer(this);
        this.postMessage(byteBuffer);
    }

    public void handleMessage(Connection conn, byte[] buffer) {
        OracleServerConnection serverConn = (OracleServerConnection) conn;
        ByteBuffer byteBuffer = null;

        if (logger.isDebugEnabled()) {
            System.out.println("========================================================");
            System.out.println("@amoeba receive from dbServer:" + ByteUtil.toHex(buffer, 0, buffer.length));
        }
        switch (buffer[4]) {
            case NS_PACKT_TYPE_RESEND:
                ConnectPacket connPacket = genConnectPacket();
                byteBuffer = connPacket.toByteBuffer(serverConn);
                if (logger.isDebugEnabled()) {
                    System.out.println("receive NS_PACKT_TYPE_RESEND packet.");
                    System.out.println("send ConnectPacket.");
                }
                break;
            case NS_PACKT_TYPE_ACCEPT:
                T4C8TTIproDataPacket proPacket = new T4C8TTIproDataPacket();
                byteBuffer = proPacket.toByteBuffer(serverConn);
                if (logger.isDebugEnabled()) {
                    System.out.println("receive NS_PACKT_TYPE_ACCEPT packet.");
                    System.out.println("send T4C8TTIproDataPacket.");
                }
                break;
            case NS_PACKT_TYPE_REDIRECT:
                RedirectPacket redirectpacket = new RedirectPacket();
                establishConnection(redirectpacket.getData());
                if (logger.isDebugEnabled()) {
                    System.out.println("receive NS_PACKT_TYPE_REDIRECT packet.");
                    System.out.println("send RedirectPacket.");
                }
                break;
            case NS_PACKT_TYPE_REFUTE:
                RefusePacket refusePacket = new RefusePacket();
                refusePacket.init(buffer, serverConn);
                this.setAuthenticated(false);
                this.postClose(null);
                if (logger.isDebugEnabled()) {
                    System.out.println("receive NS_PACKT_TYPE_REFUTE packet.");
                    System.out.println("send RefusePacket.");
                }
                break;
            case 3:
            case NS_PACKT_TYPE_DATA: {
                if (T4CTTIMsgPacket.isMsgType(buffer, T4CTTIMsgPacket.TTIPRO)) {
                    T4C8TTIproResponseDataPacket proRespPacket = new T4C8TTIproResponseDataPacket();
                    proRespPacket.init(buffer, serverConn);
                    serverConn.setProtocolField(proRespPacket);

                    T4C8TTIdtyDataPacket dtyPacket = new T4C8TTIdtyDataPacket();
                    byteBuffer = dtyPacket.toByteBuffer(serverConn);
                    if (logger.isDebugEnabled()) {
                        System.out.println("receive T4C8TTIproResponseDataPacket.");
                        System.out.println("send T4C8TTIdtyDataPacket.");
                    }

                } else if (T4CTTIMsgPacket.isMsgType(buffer, T4CTTIMsgPacket.TTIDTY)) {
                    T4C8TTIdtyResponseDataPacket dtyResppacket = new T4C8TTIdtyResponseDataPacket();
                    dtyResppacket.init(buffer, serverConn);
                    serverConn.setBasicTypes();

                    T4C7OversionDataPacket versionPacket = new T4C7OversionDataPacket();
                    byteBuffer = versionPacket.toByteBuffer(serverConn);
                    lastPacketRequest = versionPacket;
                    if (logger.isDebugEnabled()) {
                        System.out.println("receive T4C8TTIdtyResponseDataPacket.");
                        System.out.println("send T4C7OversionDataPacket.");
                    }

                } else if (lastPacketRequest instanceof T4C7OversionDataPacket) {
                    T4C7OversionResponseDataPacket versionRespPacket = new T4C7OversionResponseDataPacket();
                    versionRespPacket.init(buffer, serverConn);
                    OracleConnection.setVersionNumber(versionRespPacket.getVersionNumber());

                    T4CTTIoAuthKeyDataPacket authekeyPacket = new T4CTTIoAuthKeyDataPacket();
                    authekeyPacket.user = this.getUser();
                    authekeyPacket.pid = "1223";
                    authekeyPacket.program_nm = "amoeba proxy";
                    authekeyPacket.sid = this.getSchema();
                    authekeyPacket.terminal = "unkonw";
                    authekeyPacket.machine = "unknow";
                    byteBuffer = authekeyPacket.toByteBuffer(serverConn);
                    lastPacketRequest = authekeyPacket;
                    if (logger.isDebugEnabled()) {
                        System.out.println("receive T4C7OversionResponseDataPacket.");
                        System.out.println("send T4CTTIoAuthKeyDataPacket.");
                    }

                } else if (lastPacketRequest instanceof T4CTTIoAuthKeyDataPacket) {
                    T4CTTIoAuthKeyResponseDataPacket authkeyRespPacket = new T4CTTIoAuthKeyResponseDataPacket();
                    authkeyRespPacket.init(buffer, serverConn);

                    T4CTTIoAuthDataPacket authPacket = new T4CTTIoAuthDataPacket();
                    this.encryptedSK = authkeyRespPacket.encryptedSK;
                    authPacket.encryptedSK = authkeyRespPacket.encryptedSK;
                    authPacket.userStr = this.getUser();
                    authPacket.passwordStr = this.getPassword();

                    byteBuffer = authPacket.toByteBuffer(serverConn);
                    lastPacketRequest = authPacket;

                    if (logger.isDebugEnabled()) {
                        System.out.println("receive T4CTTIoAuthKeyResponseDataPacket.");
                        System.out.println("send T4CTTIoAuthDataPacket.");
                    }

                } else if (lastPacketRequest instanceof T4CTTIoAuthDataPacket) {
                    T4CTTIoAuthResponseDataPacket authRespPacket = new T4CTTIoAuthResponseDataPacket();
                    authRespPacket.init(buffer, conn);
                    if (authRespPacket.oer.retCode == 0) {
                        this.setAuthenticated(true);
                    }
                    byteBuffer = null;

                    lastPacketRequest = authRespPacket;
                    if (logger.isDebugEnabled()) {
                        System.out.println("receive T4CTTIoAuthResponseDataPacket.");
                    }
                }
                break;
            }
            case 7:
            case 8:
            case 9:
            case 10:
            default: {
                this.postClose(null);
                return;
            }
        }

        if (byteBuffer != null) {
            if (logger.isDebugEnabled()) {
                byte[] respMessage = byteBuffer.array();
                System.out.println("@amoeba send to dbServer:" + ByteUtil.toHex(respMessage, 0, respMessage.length));
                System.out.println();
            }
            this.postMessage(byteBuffer);
        }
    }

    private void establishConnection(String data) {

    }

    public ObjectPool getObjectPool() {
        return objectPool;
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void setObjectPool(ObjectPool pool) {
        this.objectPool = pool;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRemovedFromPool() {
        return objectPool == null;
    }

    protected void close(Exception exception) {
        super.close(exception);
        final ObjectPool tmpPool = objectPool;
        objectPool = null;
        try {
            if (tmpPool != null) {

                /**
                 * 处于active 状态的 poolableObject，可以用ObjectPool.invalidateObject 方式从pool中销毁 否则只能等待被borrow 或者 idle time out
                 */
                if (isActive()) {
                    tmpPool.invalidateObject(this);
                }
            }
        } catch (Exception e) {

        }
    }

    public ConnectPacket genConnectPacket() {
        ConnectPacket packet = new ConnectPacket();
        StringBuilder builder = new StringBuilder();
        builder.append("(DESCRIPTION=(CONNECT_DATA=(SID=");
        builder.append(this.getSchema());
        builder.append(")");
        builder.append("(CID=(PROGRAM=)(HOST=__jdbc__)(USER=)))(ADDRESS=(PROTOCOL=tcp)(HOST=");
        builder.append(this.getChannel().socket().getInetAddress().getHostAddress());
        builder.append(")(PORT=");
        builder.append(this.getChannel().socket().getPort());
        builder.append(")))");
        packet.data = builder.toString();
        return packet;
    }

}
