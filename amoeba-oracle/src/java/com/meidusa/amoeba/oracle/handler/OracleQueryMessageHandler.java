package com.meidusa.amoeba.oracle.handler;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C8OcloseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIMsgPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * 非常简单的数据包转发程序
 * 
 * @author struct
 */
public class OracleQueryMessageHandler implements MessageHandler, Sessionable, SQLnetDef {

    private static Logger    logger  = Logger.getLogger(OracleQueryMessageHandler.class);

    private OracleConnection clientConn;
    private OracleConnection serverConn;
    private MessageHandler   clientHandler;
    private MessageHandler   serverHandler;
    private boolean          isEnded = false;

    public OracleQueryMessageHandler(Connection clientConn, Connection serverConn){
        this.clientConn = (OracleConnection) clientConn;
        clientHandler = clientConn.getMessageHandler();
        this.serverConn = (OracleConnection) serverConn;
        serverHandler = serverConn.getMessageHandler();
        clientConn.setMessageHandler(this);
        serverConn.setMessageHandler(this);
    }

    public void handleMessage(Connection conn, byte[] message) {
        if (logger.isDebugEnabled()) {
            System.out.println("$receive packet:" + ByteUtil.toHex(message, 0, message.length));
        }

        if (conn == clientConn) {
//            if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OALL8)) {
//                // T4C8OallDataPacket packet = new T4C8OallDataPacket();
//                // packet.init(message, conn);
//                if (logger.isDebugEnabled()) {
//                    System.out.println("$amoeba receive from appClient:" + ByteUtil.toHex(message, 0, message.length));
//                    System.out.println("$amoeba receive T4C8OallDataPacket.");
//
//                    // System.out.println("query packet:" + T4CTTIfunPacket.OALL8);
//                    // System.out.println("sql:" + new String(packet.sqlStmt));
//                    // System.out.println("numberOfBindPositions:" + packet.numberOfBindPositions);
//                    // System.out.println("oacdefBindsSent:" + Arrays.toString(packet.oacdefBindsSent));
//                    System.out.println();
//                }
//            } else if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OFETCH)) {
//                // if (logger.isDebugEnabled()) {
//                // System.out.println("query packet:" + T4CTTIfunPacket.OFETCH);
//                // System.out.println();
//                // }
//            } else if (T4CTTIfunPacket.isFunType(message, T4CTTIMsgPacket.TTIPFN, T4CTTIfunPacket.OCCA)) {
//                // T4C8OcloseDataPacket packet = new T4C8OcloseDataPacket();
//                // packet.init(message, conn);
//                // if (logger.isDebugEnabled()) {
//                // System.out.println("query packet:T4C8OcloseDataPacket");
//                // System.out.println();
//                // }
//            }

            serverConn.postMessage(message);// proxy-->server
        } else {
            clientConn.postMessage(message);// proxy-->client
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

}
