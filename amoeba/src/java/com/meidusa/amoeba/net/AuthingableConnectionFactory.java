package com.meidusa.amoeba.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 需要等待验证得连接工厂
 * @author struct
 *
 */
public abstract class AuthingableConnectionFactory extends AbstractConnectionFactory {
	
	private long timeOut = 15000;

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	public Connection createConnection(SocketChannel channel, long createStamp) throws IOException {
		Connection connection = (Connection) super.createConnection(channel, createStamp);
		waitforAuthenticate(connection);
		return connection;
	}

	
	
	protected void waitforAuthenticate(Connection connection){
		if(connection instanceof AuthingableConnection){ 
			AuthingableConnection authconn = (AuthingableConnection)connection;
			if(this.getTimeOut()>0){
				authconn.isAuthenticatedWithBlocked(this.getTimeOut());
			}
		}else{
			connection.getConnectionManager().notifyObservers(ConnectionManager.CONNECTION_ESTABLISHED, connection, null);
		}
	}
}
