package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import org.apache.commons.pool.ObjectPool;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.oracle.context.OracleProxyRuntimeContext;
import com.meidusa.amoeba.oracle.handler.OracleMessageHandler;
import com.meidusa.amoeba.oracle.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.packet.AnoServerDataPacket;
import com.meidusa.amoeba.oracle.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.packet.Packet;
import com.meidusa.amoeba.oracle.packet.ResendPacket;

public class OracleClientConnection extends OracleConnection {

    private String     defaultPoolName = null;
    private ObjectPool pool            = null;
    private int        msgCount        = 0;

    public OracleClientConnection(SocketChannel channel, long createStamp){
        super(channel, createStamp);
        defaultPoolName = OracleProxyRuntimeContext.getInstance().getQueryRouter().getDefaultPool();
        pool = OracleProxyRuntimeContext.getInstance().getPoolMap().get(defaultPoolName);
        switchHandler();
    }

    public void handleMessage(Connection conn, byte[] message) {
    	OracleClientConnection clientConn  = (OracleClientConnection)conn;
    	
        msgCount++;

        Packet packet = null;
        if (msgCount == 1) {
            if (message[4] == Packet.NS_PACKT_TYPE_CONNECT) {
            	ConnectPacket connPacket = new ConnectPacket();
            	connPacket.init(message);
            	clientConn.setAnoEnabled(connPacket.anoEnabled);
                packet = new AcceptPacket();
            } else {
                throw new RuntimeException("Error data packet.");
            }
        }if(msgCount ==2){
        	if(clientConn.isAnoEnabled()){
        		packet = new AnoServerDataPacket(); 
        	}else{
        		
        	}
        }
        // ...

        postMessage(packet.toByteBuffer().array());
    }

    private void switchHandler() {
        try {
            Connection dst = (Connection) pool.borrowObject();
            new OracleMessageHandler(this, dst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
