package com.meidusa.amoeba.aladdin.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.meidusa.amoeba.aladdin.io.ResultPacket;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.poolable.ObjectPool;


/**
 * 
 * @author struct
 *
 */
public abstract class CommandMessageHandler implements MessageHandler, Sessionable {
	protected MysqlClientConnection source;
	private boolean completed;
	private long createTime;
	private long timeout;
	private long endTime;
	private boolean ended = false;
	private ObjectPool[] pools;
	private final Lock lock = new ReentrantLock(false);
	private Map<java.sql.Connection, ObjectPool> connPoolMap = new HashMap<java.sql.Connection, ObjectPool>();
	private String query;
	private Object parameter;
	
	protected static abstract class QueryRunnable implements Runnable{
		protected java.sql.Connection conn;
		protected Object parameter;
		protected CountDownLatch latch;
		protected ResultPacket packet;
		protected String query;
		
		QueryRunnable(CountDownLatch latch,java.sql.Connection conn,String query,Object parameter, ResultPacket packet){
			this.conn = conn;
			this.parameter = parameter;
			this.packet = packet;
			this.query = query;
			this.latch = latch;
		}
		
		protected static boolean isSelect(String query){
			char ch = query.trim().charAt(0);
			if(ch == 's' || ch == 'S'){
				return true;
			}else{
				return false;
			}
		}
		
		protected abstract void doRun();
		
		public void run() {
			try{
				doRun();
			}finally{
				latch.countDown();
			}
		}
	}
	
	public CommandMessageHandler(MysqlClientConnection source, String query,Object parameter,
			ObjectPool[] pools, long timeout) {
		this.source = source;
		this.query = query;
		this.pools = pools;
		this.timeout = timeout;
		this.parameter = parameter;
	}

	public void handleMessage(Connection conn, byte[] message) {
		// TODO Auto-generated method stub

	}

	public boolean checkIdle(long now) {
		if (timeout > 0) {
			return (now - createTime) > timeout;
		} else {
			if (ended) {
				/**
				 * 如果该session已经结束，此时如果serverConnection端还在等待所有数据访问。并且超过15s,
				 * 则需要当空闲的会话
				 * 避免由于各种原因造成服务器端没有发送数据或者已经结束的会话而ServerConnection无法返回Pool中。
				 */
				return (now - endTime) > 15000;
			}
			return false;
		}
	}

	public void startSession() throws Exception {
		final CountDownLatch latch = new CountDownLatch(pools.length);
		ResultPacket packet = newResultPacket(query);
		for (ObjectPool pool : pools) {
			final java.sql.Connection conn = (java.sql.Connection) pool
					.borrowObject();
			connPoolMap.put(conn, pool);
			QueryRunnable runnable = newQueryRunnable(latch,conn,query,parameter,packet);
			ProxyRuntimeContext.getInstance().getServerSideExecutor().execute(runnable);
		}
		
		if(timeout>0){
			latch.await(timeout,TimeUnit.MILLISECONDS);
		}else{
			latch.await();
		}
		packet.wirteToConnection(source);
	}

	protected abstract ResultPacket newResultPacket(String query);
	protected abstract QueryRunnable newQueryRunnable(CountDownLatch latch, java.sql.Connection conn, String query2,Object parameter, ResultPacket packet);
	public void endSession() {
		lock.lock();
		try {
			if (!ended) {
				for (Map.Entry<java.sql.Connection, ObjectPool> entry : connPoolMap.entrySet()) {
					try {
						entry.getValue().returnObject(entry.getKey());
					} catch (Exception e) {
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean isEnded() {
		lock.lock();
		try {
			return this.ended;
		} finally {
			lock.unlock();
		}
	}

}
