package com.meidusa.amoeba.aladdin.handler;

import com.meidusa.amoeba.mysql.handler.PreparedStatmentInfo;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.ExecutePacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;


/**
 * 
 * @author struct
 *
 */

public class PreparedStatmentExecuteMessageHandler implements MessageHandler {
	private MysqlClientConnection sourceConn;
	private PreparedStatmentInfo preparedInf;
	private ExecutePacket executePacket;
	private ObjectPool[] pools;
	private long timeout;
	
	public PreparedStatmentExecuteMessageHandler(MysqlClientConnection conn,
			PreparedStatmentInfo preparedInf, ExecutePacket executePacket,
			ObjectPool[] pools, long timeout) {
		this.sourceConn = conn;
		this.preparedInf = preparedInf;
		this.executePacket = executePacket;
		this.pools = pools;
		this.timeout = timeout;
	}

	public void handleMessage(Connection conn, byte[] message) {
		// TODO Auto-generated method stub

	}

}
