package com.meidusa.amoeba.aladdin.handler;

import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;

/**
 * 
 * @author struct
 *
 */
public class QueryCommandMessageHandler implements MessageHandler {
	
	
	public QueryCommandMessageHandler(MysqlClientConnection source,String query,ObjectPool[] pools,long timeout){
		
	}
	
	public void handleMessage(Connection conn, byte[] message) {

	}

}
