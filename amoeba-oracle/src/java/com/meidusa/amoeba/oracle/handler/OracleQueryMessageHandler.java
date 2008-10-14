package com.meidusa.amoeba.oracle.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.oracle.net.OracleClientConnection;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.net.OracleServerConnection;
import com.meidusa.amoeba.oracle.net.packet.DataPacket;
import com.meidusa.amoeba.oracle.net.packet.MarkerPacket;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C8OallDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8OallResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.assist.T4C8TTILob;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * @author struct
 */
@SuppressWarnings("unchecked")
public class OracleQueryMessageHandler extends AbstractMessageQueuedHandler implements MessageHandler, SQLnetDef {

    private static Logger                                         logger              = Logger.getLogger(OracleQueryMessageHandler.class);

    private OracleConnection                                      clientConn;
    private MessageHandler                                        clientHandler;
    private boolean                                               isEnded             = false;

    private final Lock                                            lock                = new ReentrantLock(false);
    protected Map<OracleServerConnection, ConnectionServerStatus> connStatusMap       = new HashMap<OracleServerConnection, ConnectionServerStatus>();
    protected Map<OracleServerConnection, MessageHandler>         handlerMap          = new HashMap<OracleServerConnection, MessageHandler>();
    private ObjectPool[]                                          pools;
    private OracleServerConnection[]                              serverConns;

    private boolean                                               isFirstClientPacket = true;
    private byte[]                                                tmpBuffer           = null;

    private static AtomicLong                                     startCount          = new AtomicLong(0L);
    private static AtomicLong                                     endCount            = new AtomicLong(0L);

    public OracleQueryMessageHandler(Connection clientConn, ObjectPool[] pools){
        this.clientConn = (OracleConnection) clientConn;
        this.clientHandler = clientConn.getMessageHandler();
        this.pools = pools;
        this.clientConn.setMessageHandler(this);
    }

    public void doHandleMessage(Connection conn, byte[] message) {
        if (conn == clientConn) {
            receivePrint(logger.isDebugEnabled(), message, true);

            if (MarkerPacket.isMarkerType(message)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>receive marker packet from client!");
                }
                message[10] = 2;
                sendPrint(logger.isDebugEnabled(), message, false);
                OracleClientConnection occonn = (OracleClientConnection) conn;
                occonn.postMessage(message);
                return;
            }

            // 解析数据包，否则直接传送数据包到服务器端。
            if (DataPacket.isDataType(message) && !DataPacket.isDataEOF(message)) {
                // 合并完成，进行数据包解析。
                if (mergeClientMessage(message)) {
                    if (T4C8OallDataPacket.isParseable(tmpBuffer)) {
                        OracleClientConnection occonn = (OracleClientConnection) conn;
                        T4C8OallDataPacket packet = new T4C8OallDataPacket();
                        packet.init(tmpBuffer, conn);

                        if (occonn.isLobOps()) {
                            for (int i = 0; i < serverConns.length; i++) {
                                connStatusMap.get(serverConns[i]).setLobOps(true);
                                connStatusMap.get(serverConns[i]).setLob(packet.getLob());
                            }
                            occonn.setLobOps(false);
                        } else {
                            if (packet.isOlobops()) {
                                occonn.setLobOps(true);

                                int offset = packet.getLob().getSourceLobLocatorOffset();
                                byte[] abyte0 = new byte[T4C8TTILob.LOB_OPS_BYTES];
                                System.arraycopy(tmpBuffer, offset, abyte0, 0, abyte0.length);
                                int rowIndex = ByteUtil.toInt32BE(abyte0, 0);
                                byte[] realBytes = occonn.getLobLocaterMap().get(rowIndex);
                                System.arraycopy(realBytes, 0, tmpBuffer, offset, realBytes.length);
                                message = tmpBuffer;
                                for (int i = 0; i < serverConns.length; i++) {
                                    connStatusMap.get(serverConns[i]).setLobOps(true);
                                    connStatusMap.get(serverConns[i]).setLob(packet.getLob());
                                }
                            }
                        }
                    }
                    tmpBuffer = null;
                }
            }

            for (int i = 0; i < serverConns.length; i++) {
                sendPrint(logger.isDebugEnabled(), message, true);
                serverConns[i].postMessage(message);
            }
        } else {
            receivePrint(logger.isDebugEnabled(), message, false);

            if (MarkerPacket.isMarkerType(message)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("<<receive marker packet from server!");
                }
                message[10] = 2;
                OracleServerConnection osconn = (OracleServerConnection) conn;
                sendPrint(logger.isDebugEnabled(), message, true);
                osconn.postMessage(message);
                return;
            }

            if (T4C8OallResponseDataPacket.isParseable(message)) {
                // 解析和合并服务器端数据包
                message = parseServerPakcet(message, conn);

                // 切分数据包，并发送给客户端。
                byte[][] msgList = splitServerMessage(message);
                for (int i = 0; i < msgList.length; i++) {
                    sendPrint(logger.isDebugEnabled(), msgList[i], false);
                    clientConn.postMessage(msgList[i]);
                }
            } else {
                sendPrint(logger.isDebugEnabled(), message, false);
                clientConn.postMessage(message);
            }
        }
    }

    public boolean checkIdle(long now) {
        return false;
    }

    public void startSession() throws Exception {
        serverConns = new OracleServerConnection[pools.length];
        for (int i = 0; i < pools.length; i++) {
            ObjectPool pool = pools[i];
            OracleServerConnection conn = (OracleServerConnection) pool.borrowObject();
            if (logger.isInfoEnabled()) {
                logger.info("startSession:" + startCount.incrementAndGet());
            }
            if (logger.isDebugEnabled()) {
                int h = conn.hashCode();
                logger.debug("");
                logger.debug("+++++++++++++++++++++++++ borrowed conn[" + h + "] from pool +++++++++++++++++++++++++");
            }
            serverConns[i] = conn;
            handlerMap.put(conn, conn.getMessageHandler());
            connStatusMap.put(conn, new ConnectionServerStatus(conn));
            conn.setMessageHandler(this);
        }
    }

    public synchronized void endSession() {
        lock.lock();

        try {
            if (!isEnded) {
                isEnded = true;
                clientConn.setMessageHandler(clientHandler);

                Set<Map.Entry<OracleServerConnection, MessageHandler>> handlerSet = handlerMap.entrySet();
                for (Map.Entry<OracleServerConnection, MessageHandler> entry : handlerSet) {
                    OracleServerConnection conn = entry.getKey();

                    ConnectionServerStatus status = connStatusMap.get(conn);
                    if (status != null && status.isCompleted) {
                        releaseConn(conn);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void releaseConn(OracleServerConnection conn) {
        lock.lock();
        try {
            MessageHandler handler = handlerMap.get(conn);
            conn.setMessageHandler(handler);
            if (!conn.isClosed()) {
                PoolableObject pooledObject = (PoolableObject) conn;
                if (pooledObject.getObjectPool() != null) {
                    try {
                        pooledObject.getObjectPool().returnObject(conn);
                        if (logger.isInfoEnabled()) {
                            logger.info("endSession:" + endCount.incrementAndGet());
                        }
                        if (logger.isDebugEnabled()) {
                            int h = conn.hashCode();
                            logger.debug("");
                            logger.debug("------------------------- returned conn[" + h + "] to pool ---------------------------\n");
                        }
                    } catch (Exception e) {
                        logger.error("OracleQueryMessageHandler endSession error", e);
                    }

                }
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isEnded() {
        lock.lock();
        try {
            return isEnded;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 合并客户端数据包
     */
    private boolean mergeClientMessage(byte[] message) {
        boolean isPacketEOF = DataPacket.isPacketEOF(message);
        if (isFirstClientPacket) {
            tmpBuffer = message;
            if (!isPacketEOF) {
                isFirstClientPacket = false;
            }
        } else {
            int headSize = OraclePacketConstant.DATA_PACKET_HEADER_SIZE;
            int appendSize = message.length - headSize;
            byte[] newBytes = new byte[tmpBuffer.length + appendSize];
            System.arraycopy(tmpBuffer, 0, newBytes, 0, tmpBuffer.length);
            System.arraycopy(message, headSize, newBytes, tmpBuffer.length, appendSize);
            tmpBuffer = newBytes;
            if (isPacketEOF) {
                isFirstClientPacket = true;
            }
        }
        return isPacketEOF;
    }

    /**
     * 解析和合并服务器端的SQL数据包
     */
    private byte[] parseServerPakcet(byte[] message, Connection conn) {
        OracleServerConnection osconn = (OracleServerConnection) conn;
        ConnectionServerStatus status = connStatusMap.get(osconn);
        OracleClientConnection occonn = (OracleClientConnection) clientConn;
        T4C8OallResponseDataPacket packet = new T4C8OallResponseDataPacket(status, occonn);
        packet.init(message, osconn);

        if (connStatusMap.size() > 0) {
            lock.lock();
            try {
                boolean allCompleted = true;
                for (Map.Entry<OracleServerConnection, ConnectionServerStatus> entry : connStatusMap.entrySet()) {
                    if (!entry.getValue().isCompleted()) {
                        allCompleted = false;
                        break;
                    } else {
                        if (this.isEnded()) {
                            releaseConn(entry.getKey());
                        }
                    }
                }

                if (allCompleted) {// 解析完成，准备合并数据包。
                    packet = mergeServerPacket();
                    endSession();
                }
            } finally {
                lock.unlock();
            }
        }

        // 写出合并后的服务器端数据包。
        return packet.toByteBuffer(osconn).array();
    }

    private T4C8OallResponseDataPacket mergeServerPacket() {
        T4C8OallResponseDataPacket packet = null;
        // 这里进行多个T4C8OallResponseDataPacket合并
        for (Map.Entry<OracleServerConnection, ConnectionServerStatus> entry : connStatusMap.entrySet()) {
            packet = entry.getValue().getPacket();
        }
        return packet;
    }

    /**
     * 切割服务器端消息
     */
    private byte[][] splitServerMessage(byte[] message) {
        if (message.length > RECEIVE_SDU) {
            int headSize = OraclePacketConstant.DATA_PACKET_HEADER_SIZE;
            int dataSize = message.length - headSize;
            int perSize = RECEIVE_SDU - headSize;
            int arraySize = (dataSize / perSize) + ((dataSize % perSize) == 0 ? 0 : 1);
            byte[][] abyte0 = new byte[arraySize][];
            int position = headSize;
            for (int i = 0; i < abyte0.length; i++) {
                if (i == abyte0.length - 1) {
                    abyte0[i] = new byte[dataSize % perSize + headSize];
                } else {
                    abyte0[i] = new byte[RECEIVE_SDU];
                }
                System.arraycopy(message, position, abyte0[i], headSize, abyte0[i].length - headSize);
                position += abyte0[i].length - headSize;
                abyte0[i][0] = (byte) ((abyte0[i].length >>> 8) & 0xff);
                abyte0[i][1] = (byte) (abyte0[i].length & 0xff);
                abyte0[i][4] = (byte) (NS_PACKT_TYPE_DATA);
            }
            return abyte0;
        } else {
            return new byte[][] { message };
        }
    }

    private void receivePrint(boolean isEnabled, byte[] message, boolean isClient) {
        if (isEnabled) {
            int size = ((message[0] & 0xff) << 8) | (message[1] & 0xff);
            logger.debug("");
            logger.debug("#amoeba query message ==============================================================");
            if (isClient) {
                logger.debug(">>receive from client[" + size + "]:" + ByteUtil.toHex(message, 0, message.length));
            } else {
                logger.debug("<<receive from server[" + size + "]:" + ByteUtil.toHex(message, 0, message.length));
            }
        }
    }

    private void sendPrint(boolean isEnabled, byte[] message, boolean isClient) {
        if (isEnabled) {
            int size = ((message[0] & 0xff) << 8) | (message[1] & 0xff);
            logger.debug("#amoeba query message ==============================================================");
            if (isClient) {
                logger.debug(">>send to server[" + size + "]:" + ByteUtil.toHex(message, 0, message.length));
            } else {
                logger.debug("<<send to client[" + size + "]:" + ByteUtil.toHex(message, 0, message.length));
            }
        }
    }

    public static class ConnectionServerStatus {

        private OracleServerConnection     conn;

        private boolean                    isCompleted;
        private T4C8OallResponseDataPacket packet;

        private int                        nbOfCols;
        private short[]                    dataType;

        private boolean                    isLobOps;
        private T4C8TTILob                 lob;

        public ConnectionServerStatus(OracleServerConnection conn){
            this.conn = conn;
        }

        public OracleServerConnection getConn() {
            return conn;
        }

        public T4C8TTILob getLob() {
            return lob;
        }

        public void setLob(T4C8TTILob lob) {
            this.lob = lob;
        }

        public boolean isLobOps() {
            return isLobOps;
        }

        public void setLobOps(boolean isLobOps) {
            this.isLobOps = isLobOps;
        }

        public short[] getDataType() {
            return dataType;
        }

        public void setDataType(short[] dataType) {
            this.dataType = dataType;
        }

        public T4C8OallResponseDataPacket getPacket() {
            return packet;
        }

        public void setPacket(T4C8OallResponseDataPacket packet) {
            this.packet = packet;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public void setCompleted(boolean isCompleted) {
            this.isCompleted = isCompleted;
        }

        public void setNbOfCols(int nbOfCols) {
            this.nbOfCols = nbOfCols;
        }

        public int getNbOfCols() {
            return nbOfCols;
        }
    }

}
