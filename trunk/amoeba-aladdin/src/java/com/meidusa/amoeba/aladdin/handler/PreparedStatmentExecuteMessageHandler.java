package com.meidusa.amoeba.aladdin.handler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import com.meidusa.amoeba.aladdin.handler.CommandMessageHandler.QueryRunnable;
import com.meidusa.amoeba.aladdin.io.MysqlResultSetPacket;
import com.meidusa.amoeba.aladdin.io.MysqlSimpleResultPacket;
import com.meidusa.amoeba.aladdin.io.PreparedResultPacket;
import com.meidusa.amoeba.aladdin.io.ResultPacket;
import com.meidusa.amoeba.aladdin.io.ResultSetUtil;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.handler.PreparedStatmentInfo;
import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.BindValue;
import com.meidusa.amoeba.mysql.net.packet.ExecutePacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;


/**
 * 
 * @author struct
 *
 */

public class PreparedStatmentExecuteMessageHandler extends CommandMessageHandler {
	private MysqlClientConnection sourceConn;
	private PreparedStatmentInfo preparedInf;
	private ExecutePacket executePacket;
	private ObjectPool[] pools;
	private long timeout;
	
	protected static class PreparedExecuteQueryRunnable extends QueryRunnable{
		private ExecutePacket executePacket;
		PreparedExecuteQueryRunnable(CountDownLatch latch, java.sql.Connection conn,
				String query, Object parameter, ResultPacket packet) {
			super(latch, conn, query, parameter, packet);
		}

		public void init(CommandMessageHandler handler){
			super.init(handler);
			executePacket = ((PreparedStatmentExecuteMessageHandler)handler).executePacket;
		}
		
		@Override
		protected void doRun(java.sql.Connection conn) {
			try{
				PreparedStatement pst = null;
				ResultSet rs = null;
				try {
					pst = conn.prepareStatement(query);
					int i=1;
					for(BindValue value : executePacket.values){
						if(!value.isNull){
							pst.setObject(i++, value.value, MysqlDefs.mysqlToJavaType(value.bufferType));
						}else{
							pst.setObject(i++,value.value);
						}
					}
					if(isSelect(query)){
						rs = pst.executeQuery();
						MysqlResultSetPacket resultPacket = (MysqlResultSetPacket)packet;
						ResultSetUtil.resultSetToPacket(resultPacket,rs);
					}else{
						MysqlSimpleResultPacket simplePacket = (MysqlSimpleResultPacket)packet;
						simplePacket.addResultCount(pst.executeUpdate());
					}
				} catch (SQLException e) {
					packet.setError(e.getErrorCode(), e.getMessage());
				}finally{
					if(rs!= null){
						try {
							rs.close();
						} catch (SQLException e) {
						}
					}
					
					if(pst != null){
						try {
							pst.close();
						} catch (SQLException e) {
						}
					}
				}
				
			}finally{
				latch.countDown();
			}
		}
	}
	
	public PreparedStatmentExecuteMessageHandler(MysqlClientConnection conn,
			PreparedStatmentInfo preparedInf,ExecutePacket executePacket,
			ObjectPool[] pools, long timeout) {
		super(conn,preparedInf.getPreparedStatment(),preparedInf,pools,timeout);
		this.sourceConn = conn;
		this.preparedInf = preparedInf;
		this.executePacket = executePacket;
		executePacket.getParameters();
		this.pools = pools;
		this.timeout = timeout;
	}

	@Override
	protected QueryRunnable newQueryRunnable(CountDownLatch latch,
			java.sql.Connection conn, String query2, Object parameter,
			ResultPacket packet) {
		return null;
	}
	

	@Override
	protected ResultPacket newResultPacket(String query) {
		if(PreparedExecuteQueryRunnable.isSelect(query)){
			return new MysqlResultSetPacket(query);
		}else{
			return new MysqlSimpleResultPacket();
		}
	}

}
