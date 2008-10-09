package com.meidusa.amoeba.oracle.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.oracle.net.OracleClientConnection;
import com.meidusa.amoeba.oracle.net.packet.DataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8OallDataPacket;
import com.meidusa.amoeba.oracle.net.packet.assist.T4C8TTILob;
import com.meidusa.amoeba.oracle.util.ByteUtil;
import com.meidusa.amoeba.route.QueryRouter;

public class OracleQueryDispatcher implements MessageHandler {

    private static final byte[] logoffBytes         = new byte[] { 0x00, 0x0b, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09 };

    protected static Logger     logger              = Logger.getLogger(OracleQueryDispatcher.class);

    private boolean             isFirstClientPacket = true;
    private List<byte[]>        listBuffer          = new ArrayList<byte[]>();
    private byte[]              tmpBuffer           = null;

    public OracleQueryDispatcher(OracleClientConnection clientConn){
        clientConn.setMessageHandler(this);
    }

    public void handleMessage(Connection conn, byte[] message) {
        listBuffer.add(message);

        if (DataPacket.isDataType(message) && !DataPacket.isDataEOF(message)) {
            if (mergeClientMessage(message)) {// 合并完成，进行数据包解析。
                if (T4C8OallDataPacket.isParseable(tmpBuffer)) {
                    T4C8OallDataPacket packet = new T4C8OallDataPacket();
                    packet.init(tmpBuffer, conn);
                    if (packet.isSqlPacket()) {// 处理SQL语句数据包的 pool路由
                        String sql = packet.sqlStmt;
                        Object[] params = new Object[packet.getParamBytes().length];
                        for (int i = 0; i < params.length; i++) {
                            params[i] = packet.accessors[i].getObject(packet.getParamBytes()[i]);
                        }
                        QueryRouter rt = ProxyRuntimeContext.getInstance().getQueryRouter();
                        ObjectPool[] op = rt.doRoute((DatabaseConnection) conn, sql, false, params);
                        startOracleQueryMessageHandler(conn, op);
                        return;
                    } else if (packet.isOlobops()) {// 处理请求LOB数据包包的pool路由
                        QueryRouter rt = ProxyRuntimeContext.getInstance().getQueryRouter();
                        int offset = packet.getLob().getSourceLobLocatorOffset();
                        byte[] abyte0 = new byte[T4C8TTILob.LOB_OPS_BYTES];
                        System.arraycopy(tmpBuffer, offset, abyte0, 0, abyte0.length);
                        int poolHashCode = ByteUtil.toInt32BE(abyte0, 4);

                        ObjectPool[] op = new ObjectPool[] { rt.getObjectPool(poolHashCode) };
                        startOracleQueryMessageHandler(conn, op);
                        return;
                    } else if (packet.isOlogoff()) {// 处理logoff数据包
                        conn.postMessage(logoffBytes);
                        if (logger.isDebugEnabled()) {
                            int size = ((tmpBuffer[0] & 0xff) << 8) | (tmpBuffer[1] & 0xff);
                            System.out.println("%amoeba query message ==============================================================");
                            System.out.println(">>receive from client[" + size + "]:" + ByteUtil.toHex(tmpBuffer, 0, tmpBuffer.length));
                            System.out.println("<<amoeba query message =============================================================");
                            System.out.println("<<send to client[" + logoffBytes.length + "]:" + ByteUtil.toHex(logoffBytes, 0, logoffBytes.length));
                        }
                        clearBuffer();
                        return;
                    } else {
                        if (logger.isDebugEnabled()) {
                            System.out.println("warning!unprocess data packet.");
                        }
                    }
                }
            } else {
                return;
            }
        }

        clearBuffer();
    }

    private void startOracleQueryMessageHandler(Connection conn, ObjectPool[] op) {
        OracleQueryMessageHandler handler = new OracleQueryMessageHandler(conn, op);
        try {
            handler.startSession();
            for (byte[] msg : listBuffer) {
                handler.handleMessage(conn, msg);
            }
        } catch (Exception e) {
            logger.error("OracleQueryMessageHandler startSession error", e);
            handler.endSession();
        } finally {
            clearBuffer();
        }
    }

    /**
     * 清除缓冲
     */
    private void clearBuffer() {
        tmpBuffer = null;
        listBuffer.clear();
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

}
