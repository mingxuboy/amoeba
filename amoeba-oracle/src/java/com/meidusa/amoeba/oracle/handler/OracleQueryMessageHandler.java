package com.meidusa.amoeba.oracle.handler;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.net.packet.DataPacket;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.SimpleDataPacket;
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

    private static Logger    logger         = Logger.getLogger(OracleQueryMessageHandler.class);

    private OracleConnection clientConn;
    private OracleConnection serverConn;
    private MessageHandler   clientHandler;
    private MessageHandler   serverHandler;
    private boolean          isEnded        = false;

    private byte[]           tmpReceiveData = new byte[0];                                       ;

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

            copyDataToTmp(message);
            if (DataPacket.isPacketEOF(message)) {
                if (logger.isDebugEnabled()) {
                    System.out.println("\n$amoeba query message ========================================================");
                    System.out.println("$send packet:" + ByteUtil.toHex(tmpReceiveData, 0, tmpReceiveData.length));
                }

                if (T4CTTIfunPacket.isFunType(tmpReceiveData, T4CTTIfunPacket.OALL8)) {
                    T4C8OallDataPacket packet = new T4C8OallDataPacket();
                    packet.init(tmpReceiveData, conn);
                    if (logger.isDebugEnabled()) {
                        System.out.println("type:T4CTTIfunPacket.OALL8");
                        System.out.println("isPacketEOF:" + packet.isPacketEOF());
                        System.out.println("sqlStmt:" + new String(packet.sqlStmt));
                        System.out.println("numberOfBindPositions:" + packet.numberOfBindPositions);
                        for (int i = 0; packet.bindParams != null && i < packet.bindParams.length; i++) {
                            System.out.println("params_" + i + ":" + ByteUtil.toHex(packet.bindParams[i], 0, packet.bindParams[i].length));
                        }
                    }
                } else if (T4CTTIfunPacket.isFunType(tmpReceiveData, T4CTTIfunPacket.OFETCH)) {
                    DataPacket dataPacket = new SimpleDataPacket();
                    dataPacket.init(tmpReceiveData, conn);
                    if (logger.isDebugEnabled()) {
                        System.out.println("type:T4CTTIfunPacket.OFETCH");
                        System.out.println("isPacketEOF:" + dataPacket.isPacketEOF());
                    }
                } else if (T4CTTIfunPacket.isFunType(tmpReceiveData, T4CTTIMsgPacket.TTIPFN, T4CTTIfunPacket.OCCA)) {
                    // T4C8OcloseDataPacket packet = new T4C8OcloseDataPacket();
                    // packet.init(message, conn);
                    DataPacket dataPacket = new SimpleDataPacket();
                    dataPacket.init(tmpReceiveData, conn);
                    if (logger.isDebugEnabled()) {
                        System.out.println("type:T4C8OcloseDataPacket");
                        System.out.println("isPacketEOF:" + dataPacket.isPacketEOF());
                    }
                } else {
                    DataPacket dataPacket = new SimpleDataPacket();
                    dataPacket.init(tmpReceiveData, conn);
                    if (logger.isDebugEnabled()) {
                        System.out.println("type:OtherPacket");
                        System.out.println("isPacketEOF:" + dataPacket.isPacketEOF());
                    }
                }
                tmpReceiveData = new byte[0];
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

    private void copyDataToTmp(byte[] message) {
        byte[] newBytes = new byte[tmpReceiveData.length + message.length];
        System.arraycopy(tmpReceiveData, 0, newBytes, 0, tmpReceiveData.length);
        System.arraycopy(message, 0, newBytes, tmpReceiveData.length, message.length);
        tmpReceiveData = newBytes;
    }

}
