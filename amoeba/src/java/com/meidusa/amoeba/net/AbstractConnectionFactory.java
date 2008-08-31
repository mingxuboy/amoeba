package com.meidusa.amoeba.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


/**
 * 抽象得连接工厂,必须设置 {@link #setConnectionManager(ConnectionManager)},
 * 这儿ConnectoinManager将负责管理从这个工厂创建出来得连接.
 * 
 * @author struct
 *
 */
public abstract class AbstractConnectionFactory implements ConnectionFactory {

	protected ConnectionManager connectionManager;
	
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	/**
	 * 创建一个连接,初始化连接,注册到连接管理器,
	 * 
	 * @return Connection 返回该连接实例
	 */
	public Connection createConnection(SocketChannel channel, long createStamp) throws IOException {
		Connection connection = (Connection) newConnectionInstance(channel,System.currentTimeMillis());
		initConnection(connection);
		connectionManager.postRegisterNetEventHandler(connection, SelectionKey.OP_READ);
		return connection;
	}
	
	/**
	 * 创建以后,在这儿将对新创建得连接做一些初始化
	 * @param connection
	 */
	protected void initConnection(Connection connection){
		
	}
	
	/**
	 * 创建连接实例
	 * @param channel
	 * @param createStamp
	 * @return
	 */
	protected abstract Connection newConnectionInstance(SocketChannel channel, long createStamp);

}
