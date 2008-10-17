/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.net;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.data.ConMgrStats;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.LoopingThread;
import com.meidusa.amoeba.util.NameableRunner;
import com.meidusa.amoeba.util.Queue;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.Tuple;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class ConnectionManager extends LoopingThread implements Reporter,Initialisable {
	protected static Logger logger = Logger.getLogger(ConnectionManager.class);
	protected static final int SELECT_LOOP_TIME = 100;

	// codes for notifyObservers()
	protected static final int CONNECTION_ESTABLISHED = 0;
	
	protected static final int CONNECTION_FAILED = 1;
	protected static final int CONNECTION_CLOSED = 2;
	protected static final int CONNECTION_AUTHENTICATE_SUCCESS = 3;
	protected static final int CONNECTION_AUTHENTICATE_FAILD = 4;
	protected Selector _selector;
	
	private Executor executor;
	
	private List<NetEventHandler> _handlers = new ArrayList<NetEventHandler>();
	protected ArrayList<ConnectionObserver> _observers = new ArrayList<ConnectionObserver>();
	
	/** Our current runtime stats. */
	protected ConMgrStats _stats, _lastStats;
	
	/** 连接已经失效或者网络断开的队列 */
	protected Queue<Tuple<Connection,Exception>> _deathq = new Queue<Tuple<Connection,Exception>>();
	
	protected Queue<Tuple<NetEventHandler,Integer>> _registerQueue = new Queue<Tuple<NetEventHandler,Integer>>();
	
	/** Counts consecutive runtime errors in select(). */
	protected int _runtimeExceptionCount;

	private long idleCheckTime = 5000; //Connection idle check per 5 second
	private long lastIdleCheckTime = 0;
	
	public void setIdleCheckTime(long idleCheckTime) {
		this.idleCheckTime = idleCheckTime;
	}

	public void appendReport(StringBuilder report, long now, long sinceLast,
			boolean reset,Level level) {
		report.append("* ").append(this.getName()).append("\n");
        report.append("- Registed Connection size: ").append(_selector.keys().size()).append("\n");;
        report.append("- packet out: ").append(_stats.msgsOut).append("\n");
        report.append("- bytesOut out: ").append(_stats.bytesOut).append("\n");
        if(reset){
        	_lastStats = (ConMgrStats)_stats.clone();
        	synchronized (this) {
				_stats.bytesIn =0;
				_stats.msgsIn =0;
				_stats.msgsOut = 0;
				_stats.bytesOut = 0;
			}
        }
        
        /*if(level == Level.DEBUG){
        	Set<SelectionKey> dumpKeys = new HashSet<SelectionKey>();
        	selectorLock.lock();
        	try{
        		Set<SelectionKey> keys = _selector.keys();
		        dumpKeys.addAll(keys);
        	}finally{
        		selectorLock.unlock();
        	}
	        for(SelectionKey key: dumpKeys){
	        	if(key.attachment() instanceof Connection){
	        		Connection conn = (Connection)key.attachment();
	        		report.append("- conn: ").append(" framingData:")
	        		.append(conn.getPacketInputStream().toString())
	        		.append(" ,outQueue:").append(conn._outQueue.size())
	        		.append(" ,messagehandler:").append(conn._handler).append("\n");
	        	}
	        	
	        	if(key.attachment() instanceof Reporter.SubReporter){
	        		Reporter.SubReporter reporter = (Reporter.SubReporter)key.attachment();
	        		reporter.appendReport(report, now, sinceLast, reset,level);
	        	}
	        }
        }*/
	}

	public ConnectionManager() throws IOException {
		_selector = SelectorProvider.provider().openSelector();
		// create our stats record
		_stats = new ConMgrStats();
		_lastStats = new ConMgrStats();
	}
	
	public ConnectionManager(String managerName) throws IOException {
		super(managerName);
		_selector = SelectorProvider.provider().openSelector();

		// create our stats record
		_stats = new ConMgrStats();
		_lastStats = new ConMgrStats();
		this.setDaemon(true);
	}

	/**
	 * Performs the select loop. This is the body of the conmgr thread.
	 */
	protected void iterate() {
		final long iterStamp = System.currentTimeMillis();

		// 关闭已经断开或者宣布死亡的Connection
		Tuple<Connection,Exception> deathTuple;
		while ((deathTuple = _deathq.getNonBlocking()) != null) {
			deathTuple.left.close(deathTuple.right);
		}
		
		if(idleCheckTime>0 && iterStamp - lastIdleCheckTime>= idleCheckTime){
			lastIdleCheckTime = iterStamp;
			// 关闭空闲时间过长的连接
			for (NetEventHandler handler : _handlers) {
				if (handler.checkIdle(iterStamp)) {
					// this will queue the connection for closure on our next tick
					if(handler instanceof Connection){
						closeConnection((Connection) handler,null);
					}
				}
			}
		}
		
		//将注册的连接加入handler map中
		Tuple<NetEventHandler,Integer> registerHandler = null;
		while ((registerHandler = _registerQueue.getNonBlocking()) != null) {
			
			if(registerHandler.left instanceof Connection){
				Connection  connection = (Connection)registerHandler.left;
				this.registerConnection(connection, registerHandler.right.intValue());
				_handlers.add(connection);
			}else{
				_handlers.add(registerHandler.left);
			}
		}
		
		//检查网络事件
		Set<SelectionKey> ready = null;
		try {
			// check for incoming network events
			int ecount = _selector.select(SELECT_LOOP_TIME);
			//selectorLock.lock();
			//try{
				ready = _selector.selectedKeys();
			//}finally{
			//	selectorLock.unlock();
			//}
			if (ecount == 0) {
				if (ready.size() == 0) {
					return;
				} else {
					logger.warn("select() returned no selected sockets, but there are "
									+ ready.size() + " in the ready set.");
				}
			}

		} catch (IOException ioe) {
			logger.warn("Failure select()ing.", ioe);
			return;
		} catch (RuntimeException re) {
			// instead of looping indefinitely after things go pear-shaped, shut
			// us down in an
			// orderly fashion
			logger.warn("Failure select()ing.", re);
			if (_runtimeExceptionCount++ >= 20) {
				logger.warn("Too many errors, bailing.");
				shutdown();
			}
			return;
		}
		// clear the runtime error count
		_runtimeExceptionCount = 0;
		
		
		
		final CountDownLatch latch = new CountDownLatch(ready.size());
		//处理事件（网络数据流交互等）
		for (SelectionKey selkey : ready) {
			NetEventHandler handler = null;
			handler = (NetEventHandler)selkey.attachment();
			if (handler == null) {
				latch.countDown();
				logger.warn("Received network event but have no registered handler "
								+ "[selkey=" + selkey + "].");
				selkey.cancel();
				continue;
			}
			
			if(selkey.isWritable()){
				try{
					boolean finished = handler.doWrite();
					if(finished){
						selkey.interestOps(selkey.interestOps() & ~SelectionKey.OP_WRITE);
					}
				} catch (Exception e) {
					logger.warn("Error processing network data: " + handler + ".", e);

					if (handler != null && handler instanceof Connection) {
						closeConnection((Connection) handler,e);
					}
				}finally{
					latch.countDown();
				}
			}else if(selkey.isReadable() || selkey.isAcceptable()){
				final NetEventHandler tmpHandler = handler;

				executor.execute(new NameableRunner(){
					public void run(){
						try{
							tmpHandler.handleEvent(iterStamp);
							/*if (got != 0) {
								lock.lock();
								try{
									_stats.bytesIn += got;
									_stats.msgsIn++;
								}finally{
									lock.unlock();
								}
							}*/
						}finally{
							latch.countDown();
						}
					}

					public String getRunnerName() {
						return ConnectionManager.this.getName()+"-Reading";
					}
				});
			}else{
				latch.countDown();
				logger.error(selkey.attachment()+", isAcceptable="+selkey.isAcceptable()+",isConnectable="+selkey.isConnectable()+",isReadable="+selkey.isReadable()+",isWritable="+selkey.isWritable());
			}
		}
		
		ready.clear();
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * 采用异步方式关闭一个连接。
	 * 将即将关闭的连接放入deathQueue中
	 * @param conn
	 */
	void closeConnection(Connection conn,Exception exception) {
		if(!conn.isClosed()){
			_deathq.append(new Tuple<Connection,Exception>(conn,exception));
		}
	}
	
	public void closeAll() {
		synchronized(_selector){
			Set<SelectionKey> keys = _selector.keys();
			for(SelectionKey key: keys){
				Object object = key.attachment();
				if(object instanceof Connection){
					Connection conn = (Connection)object;
					closeConnection(conn,null);
				}
			}
		}
	}
	
	/**
	 * 增加 ConnectionObserver。监听Connection 相关的网络事件
	 * @see ConnectionObserver
	 */
	public void addConnectionObserver(ConnectionObserver observer) {
		synchronized (_observers) {
			_observers.add(observer);
		}
	}

	/**
	 * 从 Observer 列表中删除一个Observer对象
	 * @param observer
	 */
	public void removeConnectionObserver(ConnectionObserver observer) {
		synchronized (_observers) {
			_observers.remove(observer);
		}
	}

	protected void notifyObservers(int code, Connection conn, Object arg1) {
		synchronized (_observers) {
			for (ConnectionObserver obs : _observers) {
				switch (code) {
				case CONNECTION_ESTABLISHED:
					obs.connectionEstablished(conn);
					break;
				case CONNECTION_FAILED:
					obs.connectionFailed(conn, (Exception) arg1);
					break;
				case CONNECTION_CLOSED:
					obs.connectionClosed(conn);
					break;
				default:
					throw new RuntimeException(
							"Invalid code supplied to notifyObservers: " + code);
				}
			}
		}
	}

	/**
	 * 异步注册一个NetEventHandler
	 * @param connection
	 * @param key
	 */
	public void postRegisterNetEventHandler(NetEventHandler handler,int key){
		_registerQueue.append(new Tuple<NetEventHandler,Integer>(handler,key));
		/**
		 * 唤醒ConnectionManager正在等待select的线程，让其能够更快速的处理registerQueue队列中的对象
		 */
		_selector.wakeup();
	}
	/**
	 * 往ConnectionManager 增加一个SocketChannel
	 * @param channel
	 * @param key
	 * @param handler
	 */
	public void registerConnection(Connection connection,int key){
		SocketChannel channel = connection.getChannel();
		if(logger.isDebugEnabled()){
			logger.debug("["+this.getName()+"] registed Connection["+channel.socket().getInetAddress().getHostAddress()+":"+channel.socket().getPort()+"] connected!");
		}
		SelectionKey selkey = null;
		try {
			if (!(channel instanceof SelectableChannel)) {
				try {
					logger.warn("Provided with un-selectable socket as result of accept(), can't "
									+ "cope [channel=" + channel + "].");
				} catch (Error err) {
					logger.warn("Un-selectable channel also couldn't be printed.");
				}
				// stick a fork in the socket
				if(channel != null){
					channel.socket().close();
				}
				return;
			}

			SelectableChannel selchan = (SelectableChannel) channel;
			selchan.configureBlocking(false);
			selkey = selchan.register(_selector,key,connection);
			connection.setConnectionManager(this);
			connection.setSelectionKey(selkey);
			configConnection(connection);
			_stats.connects.incrementAndGet();
			connection.init();
			_selector.wakeup();
			return;
		} catch (IOException ioe) {
			logger.error("register connection error: " + ioe);
		}
		
		if(selkey != null){
			selkey.attach(null);
			selkey.cancel();
		}
		
		// make sure we don't leak a socket if something went awry
		if (channel != null) {
			try {
				channel.socket().close();
			} catch (IOException ioe) {
				logger.warn("Failed closing aborted connection: " + ioe);
			}
		}
	}
	
	protected void configConnection(Connection connection) throws SocketException{
		connection.getChannel().socket().setSendBufferSize(ProxyRuntimeContext.getInstance().getConfig().getNetBufferSize()*1024);
		connection.getChannel().socket().setReceiveBufferSize(ProxyRuntimeContext.getInstance().getConfig().getNetBufferSize()*1024);
		connection.getChannel().socket().setTcpNoDelay(ProxyRuntimeContext.getInstance().getConfig().isTcpNoDelay());
	}
	
	/**
	 * 当 Connection 关闭以后
	 * @param conn
	 */
	protected void connectionClosed(Connection conn) {
		
		/**
		 * 删除即将被关闭的相关对象
		 */
		_handlers.remove(conn);
		
		/**
		 * 通知所有Observer列表，连接已经关闭
		 */
		notifyObservers(CONNECTION_CLOSED, conn, null);
	}

	/**
	 * 当 Connection 出现异常以后
	 * @param conn
	 * @param ioe
	 */
	protected void connectionFailed(Connection conn, Exception ioe) {
		
		_handlers.remove(conn);
		_stats.disconnects.incrementAndGet();

		/**
		 * 当发生连接异常时，通知所有Observers
		 */
		notifyObservers(CONNECTION_FAILED, conn, ioe);
	}

	/**
	 * Called by {@link Connection#doWrite} and friends when they write data over the
	 * network.
	 */
	protected final void noteWrite(int msgs, int bytes) {
		//ignore 
		/*lock.lock();
		try{
			_stats.msgsOut += msgs;
			_stats.bytesOut += bytes;
		}finally{
			lock.unlock();
		}*/
	}

	public void invokeConnectionWriteMessage(Connection connection) {
		if(connection.isClosed()) return;
		try {
			SelectionKey key = connection.getSelectionKey();
			if(!key.isValid()){
				connection.handleFailure(new java.nio.channels.CancelledKeyException());
				return;
			}
			synchronized(key){
	            if(key!= null && (key.interestOps() & SelectionKey.OP_WRITE) == 0){
	            	/**
	            	 * 发送数据，如果返回false，则表示socket send buffer 已经满了。则Selector 需要监听 Writeable event
					 */
					boolean finished = connection.doWrite();
					if(!finished){
						key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
					}
				}
			}
		} catch (IOException ioe) {
			connection.handleFailure(ioe);
		}
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void init() throws InitialisationException {
		
	}
	
}
