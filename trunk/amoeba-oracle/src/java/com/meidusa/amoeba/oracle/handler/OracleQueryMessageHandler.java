package com.meidusa.amoeba.oracle.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.net.OracleServerConnection;
import com.meidusa.amoeba.oracle.net.packet.DataPacket;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C8OallDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8OallResponseDataPacket;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleQueryMessageHandler implements MessageHandler, Sessionable, SQLnetDef {

    private static Logger logger = Logger.getLogger(OracleQueryMessageHandler.class);

    static abstract class ConnectionStatuts {

        protected Connection conn;

        public ConnectionStatuts(Connection conn){
            this.conn = conn;
        }

        /**
         * 判断从服务器端返回得数据包是否表示当前请求的结束。
         */
        public boolean isCompleted(byte[] buffer) {
            return false;
        }
    }

    private OracleConnection                                 clientConn;
    private MessageHandler                                   clientHandler;
    private boolean                                          isEnded             = false;

    private byte[]                                           tmpBuffer           = null;
    private boolean                                          isFirstClientPacket = true;

    private byte[]                                           tmpBuffer2          = null;
    private boolean                                          isFirstServerPacket = true;

    private final Lock                                       lock                = new ReentrantLock(false);
    protected Map<OracleServerConnection, ConnectionStatuts> connStatusMap       = new HashMap<OracleServerConnection, ConnectionStatuts>();
    protected Map<OracleServerConnection, MessageHandler>    handlerMap          = new HashMap<OracleServerConnection, MessageHandler>();
    private ObjectPool[]                                     pools;
    private OracleServerConnection[]                         serverConns;

    public OracleQueryMessageHandler(Connection clientConn, ObjectPool[] pools){
        this.clientConn = (OracleConnection) clientConn;
        clientHandler = clientConn.getMessageHandler();
        this.pools = pools;
        clientConn.setMessageHandler(this);
    }

    public void handleMessage(Connection conn, byte[] message) {
        if (conn == clientConn) {
            if (DataPacket.isPacketEOF(message)) {
                if (isFirstClientPacket) {
                    parseClientPakcet(message, conn);
                } else {
                    mergeClientMessage(message);
                    DataPacket.setPacketEOF(tmpBuffer, true);
                    parseClientPakcet(tmpBuffer, conn);
                    tmpBuffer = null;
                    isFirstClientPacket = true;
                }
            } else {
                mergeClientMessage(message);
            }
        } else {
            if (T4C8OallResponseDataPacket.isPacketEOF(tmpBuffer2)) {
                if (isFirstServerPacket) {
                    parseServerPakcet(message, conn);
                } else {
                    mergeServerMessage(message);
                    parseServerPakcet(tmpBuffer2, conn);
                    tmpBuffer2 = null;
                    isFirstServerPacket = true;
                }
            } else {
                mergeServerMessage(message);
            }
        }

        dispatchFromMessage(conn, message);
    }

    protected void dispatchFromMessage(Connection conn, byte[] message) {
        if (conn == clientConn) {
            for (int i = 0; i < serverConns.length; i++) {
                serverConns[i].postMessage(message);
            }
        } else {
            clientConn.postMessage(message);
        }
    }

    public boolean checkIdle(long now) {
        return false;
    }

    public synchronized void endSession() {
        if (!isEnded()) {
            isEnded = true;
            clientConn.setMessageHandler(clientHandler);

            for (int i = 0; i < serverConns.length; i++) {
                if (serverConns[i] != null) {
                    serverConns[i].setMessageHandler(handlerMap.get(serverConns[i]));
                    serverConns[i].postClose(null);
                }
            }
            clientConn.postClose(null);
        }
    }

    public boolean isEnded() {
        return isEnded;
    }

    public void startSession() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(this + " session start");
        }
        serverConns = new OracleServerConnection[pools.length];
        for (int i = 0; i < pools.length; i++) {
            ObjectPool pool = pools[i];
            OracleServerConnection conn;
            conn = (OracleServerConnection) pool.borrowObject();
            serverConns[i] = conn;
            handlerMap.put(conn, conn.getMessageHandler());
            conn.setMessageHandler(this);
        }
    }

    /**
     * 合并客户端数据包
     */
    private void mergeClientMessage(byte[] message) {
        if (!DataPacket.isDataType(message)) {
            return;
        }
        if (isFirstClientPacket) {
            tmpBuffer = new byte[message.length];
            System.arraycopy(message, 0, tmpBuffer, 0, message.length);
            isFirstClientPacket = false;
        } else {
            int appendLength = message.length - OraclePacketConstant.DATA_PACKET_HEADER_SIZE;
            byte[] newBytes = new byte[tmpBuffer.length + appendLength];
            System.arraycopy(tmpBuffer, 0, newBytes, 0, tmpBuffer.length);
            System.arraycopy(message, OraclePacketConstant.DATA_PACKET_HEADER_SIZE, newBytes, tmpBuffer.length, appendLength);
            tmpBuffer = newBytes;
        }
    }

    /**
     * 合并服务器端数据包
     */
    private void mergeServerMessage(byte[] message) {
        if (!DataPacket.isDataType(message)) {
            return;
        }
        if (isFirstServerPacket) {
            isFirstServerPacket = false;
        } else {
        }
    }

    /**
     * 解析客户端的SQL数据包
     */
    private void parseClientPakcet(byte[] message, Connection conn) {
        if (logger.isDebugEnabled()) {
            System.out.println("\n$amoeba query message ========================================================");
            System.out.println("$send packet:" + ByteUtil.toHex(message, 0, message.length));
        }

        if (DataPacket.isDataEOF(message)) {
            if (logger.isDebugEnabled()) {
                System.out.println("type:DataEOFPacket");
            }
        } else if (T4C8OallDataPacket.isParseable(message)) {
            T4C8OallDataPacket clientPacket = new T4C8OallDataPacket();
            clientPacket.init(message, conn);
        } else {
            if (logger.isDebugEnabled()) {
                System.out.println("type:OtherClientPacket");
            }
        }
    }

    /**
     * 解析服务器端的SQL数据包
     */
    private void parseServerPakcet(byte[] message, Connection conn) {
        if (logger.isDebugEnabled()) {
            System.out.println("\n%amoeba query message ========================================================");
            System.out.println("%receive packet:" + ByteUtil.toHex(message, 0, message.length));
        }

        if (T4C8OallResponseDataPacket.isParseable(message)) {
            T4C8OallResponseDataPacket serverPacket = new T4C8OallResponseDataPacket();
            serverPacket.init(message, conn);
            if (logger.isDebugEnabled()) {
                System.out.println("query has completed:" + serverPacket.isCompleted());
            }
        } else {
            if (logger.isDebugEnabled()) {
                System.out.println("type:OtherServerPacket");
            }
        }
    }

}
