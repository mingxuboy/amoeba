package com.meidusa.amoeba.aladdin.handler;

import java.util.concurrent.CountDownLatch;

import com.meidusa.amoeba.aladdin.io.PreparedResultPacket;
import com.meidusa.amoeba.aladdin.io.ResultPacket;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.handler.PreparedStatmentInfo;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.OKforPreparedStatementPacket;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.poolable.ObjectPool;

/**
 * 
 * @author struct
 *
 */
public class PreparedStatmentMessageHandler extends CommandMessageHandler {

	protected static class PreparedQueryRunnable extends QueryRunnable{

		protected PreparedQueryRunnable(CountDownLatch latch, java.sql.Connection conn,
				String query, Object parameter, ResultPacket packet) {
			super(latch, conn, query, parameter, packet);
		}

		@Override
		protected void doRun(java.sql.Connection conn) {
			try{
				int count = ProxyRuntimeContext.getInstance().getQueryRouter().parseParameterCount((DatabaseConnection)this.source, query);
				PreparedResultPacket preparedPacket = (PreparedResultPacket)packet;
				PreparedStatmentInfo preparedInfo = (PreparedStatmentInfo)parameter;
				OKforPreparedStatementPacket ok = new OKforPreparedStatementPacket();
				ok.columns = 0;
				ok.parameters = count;
				ok.packetId = 1;
				ok.statementHandlerId = preparedInfo.getStatmentId();
				//preparedInfo.setOkPrepared(ok);
				/*preparedInfo.putPreparedStatmentBuffer(ok.toByteBuffer(null).array());
				for(int i=0;i<count;i++){
					FieldPacket field = new  FieldPacket();
					field.packetId = (byte)(2+i);
					preparedInfo.putPreparedStatmentBuffer(field.toByteBuffer(null).array());
				}*/
				preparedPacket.setStatementId(preparedInfo.getStatmentId());
				preparedPacket.setParameterCount(count);
			}finally{
				latch.countDown();
			}
		}
	}
	
	public PreparedStatmentMessageHandler(MysqlClientConnection conn,
			PreparedStatmentInfo preparedInf, ObjectPool[] pools, long timeout) {
		super(conn,preparedInf.getPreparedStatment(),preparedInf,pools,timeout);
	}


	@Override
	protected QueryRunnable newQueryRunnable(CountDownLatch latch,
			java.sql.Connection conn, String query, Object parameter,
			ResultPacket packet) {
		return new PreparedQueryRunnable(latch,conn,query,parameter,packet);
	}

	@Override
	protected ResultPacket newResultPacket(String query) {
		PreparedResultPacket resultPacket = new PreparedResultPacket();
		return resultPacket;
	}

}
