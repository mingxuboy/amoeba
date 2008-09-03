package com.meidusa.amoeba.oracle.handler;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.net.packet.DataPacket;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C8OallDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIMsgPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleQueryMessageHandler implements MessageHandler, Sessionable, SQLnetDef {

    private static Logger    logger        = Logger.getLogger(OracleQueryMessageHandler.class);

    private OracleConnection clientConn;
    private OracleConnection serverConn;
    private MessageHandler   clientHandler;
    private MessageHandler   serverHandler;
    private boolean          isEnded       = false;

    private byte[]           tmpBuffer     = null;
    private boolean          isFirstPacket = true;

    public OracleQueryMessageHandler(Connection clientConn, Connection serverConn){
        this.clientConn = (OracleConnection) clientConn;
        clientHandler = clientConn.getMessageHandler();
        this.serverConn = (OracleConnection) serverConn;
        serverHandler = serverConn.getMessageHandler();
        clientConn.setMessageHandler(this);
        serverConn.setMessageHandler(this);
    }

    public void handleMessage(Connection conn, byte[] message) {
        if (conn == clientConn) {
            if (DataPacket.isPacketEOF(message)) {
                if (isFirstPacket) {
                    parseReceivePakcet(message, conn);
                } else {
                    DataPacket.setPacketEOF(tmpBuffer, true);
                    parseReceivePakcet(tmpBuffer, conn);
                    tmpBuffer = null;
                    isFirstPacket = true;
                }
            } else {
                mergeMessage(message);
            }
            serverConn.postMessage(message);
        } else {
            // if (logger.isDebugEnabled()) {
            // System.out.println("\n%amoeba query message ========================================================");
            // System.out.println("%receive packet:" + ByteUtil.toHex(message, 0, message.length));
            // }
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

    /**
     * 合并数据包并去除多余的包头信息
     */
    private void mergeMessage(byte[] message) {
        if (!DataPacket.isDataType(message)) {
            return;
        }
        if (isFirstPacket) {
            tmpBuffer = new byte[message.length];
            System.arraycopy(message, 0, tmpBuffer, 0, message.length);
            isFirstPacket = false;
        } else {
            int appendLength = message.length - OraclePacketConstant.DATA_PACKET_HEADER_SIZE;
            byte[] newBytes = new byte[tmpBuffer.length + appendLength];
            System.arraycopy(tmpBuffer, 0, newBytes, 0, tmpBuffer.length);
            System.arraycopy(message, OraclePacketConstant.DATA_PACKET_HEADER_SIZE, newBytes, tmpBuffer.length, appendLength);
            tmpBuffer = newBytes;
        }
    }

    /**
     * 解析发送的SQL数据包
     */
    private void parseReceivePakcet(byte[] message, Connection conn) {
        if (logger.isDebugEnabled()) {
            System.out.println("\n$amoeba query message ========================================================");
            System.out.println("$send packet:" + ByteUtil.toHex(message, 0, message.length));
        }

        if (isOall8(message) || isOfetch(message) || isColse(message) || isOlobops(message)) {
            T4C8OallDataPacket packet = new T4C8OallDataPacket();
            packet.init(message, conn);
        } else {
            if (logger.isDebugEnabled()) {
                System.out.println("type:OtherPacket");
            }
        }
    }

    private boolean isOall8(byte[] message) {
        return T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OALL8);
    }

    private boolean isOfetch(byte[] message) {
        return T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OFETCH);
    }

    private boolean isColse(byte[] message) {
        return T4CTTIfunPacket.isFunType(message, T4CTTIMsgPacket.TTIPFN, T4CTTIfunPacket.OCCA);
    }

    private boolean isOlobops(byte[] message) {
        return T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OLOBOPS);
    }

}
