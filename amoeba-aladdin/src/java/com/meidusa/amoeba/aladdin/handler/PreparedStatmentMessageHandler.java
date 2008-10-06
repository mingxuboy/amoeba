package com.meidusa.amoeba.aladdin.handler;

import com.meidusa.amoeba.mysql.handler.PreparedStatmentInfo;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;

/**
 * 
 * @author struct
 *
 */
public class PreparedStatmentMessageHandler implements MessageHandler {

	private MysqlClientConnection sourceConn;
	private PreparedStatmentInfo preparedInf;
	private ObjectPool[] pools;
	private long timeout;
	
	public PreparedStatmentMessageHandler(MysqlClientConnection conn,
			PreparedStatmentInfo preparedInf, ObjectPool[] pools, long timeout) {
		this.sourceConn = conn;
		this.preparedInf = preparedInf;
		this.timeout = timeout;
		this.pools = pools;
	}

	public void handleMessage(Connection conn, byte[] message) {
		
	}

}
