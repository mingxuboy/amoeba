package com.meidusa.amoeba.oracle.handler;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.oracle.net.OracleClientConnection;
import com.meidusa.amoeba.oracle.net.packet.T4C8OallDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.route.QueryRouter;

public class OracleQueryDispatcher implements MessageHandler {
	
	protected static Logger logger = Logger.getLogger(OracleQueryDispatcher.class);
	
	public OracleQueryDispatcher(OracleClientConnection clientConn){
		clientConn.setMessageHandler(this);
	}
	
	public void handleMessage(Connection conn, byte[] message) {
		if (!T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OALL8)) {
			logger.error("unkonwn message......");
			return;
	    }
		
		T4C8OallDataPacket dataPacket = new T4C8OallDataPacket();
		dataPacket.init(message, conn);
		
		QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
		
		Object[] parameters = new Object[dataPacket.getParamBytes().length];
		
		for(int i=0;i<parameters.length;i++){
			parameters[i] = dataPacket.accessors[i].getObject(dataPacket.getParamBytes()[i]);
		}
		
		ObjectPool[] pools = router.doRoute((DatabaseConnection)conn, dataPacket.sqlStmt, false, parameters);
		OracleQueryMessageHandler handler = new OracleQueryMessageHandler(conn, pools);
        try {
        	handler.startSession();
        } catch (Exception e) {
        	handler.endSession();
        }
		
	}

}
