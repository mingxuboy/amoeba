package com.meidusa.amoeba.aladdin.handler;

import java.util.concurrent.CountDownLatch;

import com.meidusa.amoeba.aladdin.io.ResultPacket;
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
public class PreparedStatmentMessageHandler extends CommandMessageHandler {

	protected static class PreparedQueryRunnable extends QueryRunnable{

		PreparedQueryRunnable(CountDownLatch latch, java.sql.Connection conn,
				String query, Object parameter, ResultPacket packet) {
			super(latch, conn, query, parameter, packet);
		}

		@Override
		protected void doRun() {
			
		}
		
	}
	
	private PreparedStatmentInfo preparedInf;

	public PreparedStatmentMessageHandler(MysqlClientConnection conn,
			PreparedStatmentInfo preparedInf, ObjectPool[] pools, long timeout) {
		super(conn,preparedInf.getPreparedStatment(),preparedInf,pools,timeout);
		this.preparedInf = preparedInf;
	}

	public void handleMessage(Connection conn, byte[] message) {
		
	}

	@Override
	protected QueryRunnable newQueryRunnable(CountDownLatch latch,
			java.sql.Connection conn, String query2, Object parameter,
			ResultPacket packet) {
		return null;
	}

	@Override
	protected ResultPacket newResultPacket(boolean isSelect) {
		return null;
	}

}
