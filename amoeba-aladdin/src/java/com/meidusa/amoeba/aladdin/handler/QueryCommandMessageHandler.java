package com.meidusa.amoeba.aladdin.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.aladdin.io.MysqlResultSetPacket;
import com.meidusa.amoeba.aladdin.io.MysqlSimpleResultPacket;
import com.meidusa.amoeba.aladdin.io.ResultPacket;
import com.meidusa.amoeba.aladdin.io.ResultSetUtil;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.poolable.ObjectPool;

/**
 * 
 * @author struct
 *
 */
public class QueryCommandMessageHandler extends CommandMessageHandler {
	private static Logger logger = Logger.getLogger(QueryCommandMessageHandler.class);
	protected static class QueryRunnable extends CommandMessageHandler.QueryRunnable{

		QueryRunnable(CountDownLatch latch, java.sql.Connection conn,String query,
				Object parameter, ResultPacket packet) {
			super(latch, conn,query, parameter, packet);
		}

		@Override
		protected void doRun() {
			if(isSelect(query)){
				Statement statement = null;
				ResultSet rs = null;
				try {
					statement = conn.createStatement();
					rs = statement.executeQuery(query);
					if(logger.isDebugEnabled()){
						logger.debug("starting query:"+query);
					}
					ResultSetUtil.resultSetToPacket((MysqlResultSetPacket)packet, rs);
				} catch (SQLException e) {
					packet.setError(e.getErrorCode(), e.getMessage());
				}finally{
					if(rs != null){
						try {
							rs.close();
						} catch (SQLException e) {
						}
					}
					
					if(statement != null){
						try {
							statement.close();
						} catch (SQLException e) {
						}
					}
				}
				
			}else{
				Statement statement = null;
				ResultSet rs = null;
				try {
					statement = conn.createStatement();
					int result = statement.executeUpdate(query);
					((MysqlSimpleResultPacket)packet).addResultCount(result);
				} catch (SQLException e) {
					packet.setError(e.getErrorCode(),e.getMessage());
				}finally{
					if(rs != null){
						try {
							rs.close();
						} catch (SQLException e) {
						}
					}
					
					if(statement != null){
						try {
							statement.close();
						} catch (SQLException e) {
						}
					}
				}
			}
		}
	}
	
	public QueryCommandMessageHandler(MysqlClientConnection source,
			String query, Object parameter, ObjectPool[] pools, long timeout) {
		super(source, query, parameter, pools, timeout);
	}

	public void handleMessage(Connection conn, byte[] message) {

	}

	@Override
	public QueryRunnable newQueryRunnable(CountDownLatch latch,
			java.sql.Connection conn, String query2, Object parameter,
			ResultPacket packet) {
		return new QueryRunnable(latch,conn,query2,parameter,packet);
	}

	@Override
	protected ResultPacket newResultPacket(String query) {
		if(QueryRunnable.isSelect(query)){
			return new MysqlResultSetPacket(query);
		}else{
			return new MysqlSimpleResultPacket();
		}
	}

}
