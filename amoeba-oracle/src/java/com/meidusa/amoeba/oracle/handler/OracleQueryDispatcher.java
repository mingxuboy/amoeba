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
import com.meidusa.amoeba.route.QueryRouter;

public class OracleQueryDispatcher implements MessageHandler {

    protected static Logger logger              = Logger.getLogger(OracleQueryDispatcher.class);

    private boolean         isFirstClientPacket = true;
    private List<byte[]>    listBuffer          = new ArrayList<byte[]>();
    private byte[]          tmpBuffer           = null;

    public OracleQueryDispatcher(OracleClientConnection clientConn){
        clientConn.setMessageHandler(this);
    }

    public void handleMessage(Connection conn, byte[] message) {
        listBuffer.add(message);

        if (DataPacket.isDataType(message) && !DataPacket.isDataEOF(message)) {
            // 合并完成，进行数据包解析。
            if (mergeClientMessage(message)) {
                if (T4C8OallDataPacket.isParseable(tmpBuffer)) {
                    T4C8OallDataPacket packet = new T4C8OallDataPacket();
                    packet.init(tmpBuffer, conn);
                    // 只处理包含有SQL语句的数据包，路由到相应的Connection pool。
                    if (packet.isSqlPacket()) {
                        String sql = packet.sqlStmt;
                        Object[] params = new Object[packet.getParamBytes().length];
                        for (int i = 0; i < params.length; i++) {
                            params[i] = packet.accessors[i].getObject(packet.getParamBytes()[i]);
                        }
                        QueryRouter rt = ProxyRuntimeContext.getInstance().getQueryRouter();
                        ObjectPool[] op = rt.doRoute((DatabaseConnection) conn, sql, false, params);
                        OracleQueryMessageHandler handler = new OracleQueryMessageHandler(conn, op);
                        try {
                            handler.startSession();
                            for (byte[] msg : listBuffer) {
                                handler.handleMessage(conn, msg);
                            }
                        } catch (Exception e) {
                            logger.error("start query:[" + packet.sqlStmt + "] error", e);
                            handler.endSession();
                        } finally {
                            clearBuffer();
                        }
                        return;
                    }
                }
            } else {
                return;
            }
        }

        clearBuffer();
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
