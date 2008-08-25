package com.meidusa.amoeba.oracle.handler;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproResponseDataPacket;
import com.meidusa.amoeba.oracle.util.DBConversion;

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

    public void setConnectionField(OracleConnection conn, T4C8TTIproResponseDataPacket packet) {
        T4C8TTIproResponseDataPacket pro = (T4C8TTIproResponseDataPacket) packet;
        short word0 = pro.oVersion;
        short word1 = pro.svrCharSet;
        short word2 = DBConversion.findDriverCharSet(word1, word0);

        try {
            DBConversion conversion = new DBConversion(word1, word2, pro.NCHAR_CHARSET);
            conn.setConversion(conversion);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        conn.getRep().setServerConversion(word2 != word1);
        conn.getRep().setVersion(word0);
        if (DBConversion.isCharSetMultibyte(word2)) {
            if (DBConversion.isCharSetMultibyte(pro.svrCharSet)) conn.getRep().setFlags((byte) 1);
            else conn.getRep().setFlags((byte) 2);
        } else {
            conn.getRep().setFlags(pro.svrFlags);
        }
    }

    public boolean isEnded() {
        return isEnded;
    }

    public void startSession() throws Exception {
    }

}
