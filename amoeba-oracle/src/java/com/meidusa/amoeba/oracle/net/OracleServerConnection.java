package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import org.apache.commons.pool.ObjectPool;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.poolable.PoolableObject;

public class OracleServerConnection extends OracleConnection implements PoolableObject{

	private ObjectPool pool;
	public OracleServerConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	public void handleMessage(Connection conn, byte[] message) {
		this.getMessageHandler().handleMessage(conn, message);
	}

	public ObjectPool getObjectPool() {
		return pool;
	}

	public boolean isActive() {
		return false;
	}

	public boolean isRemovedFromPool() {
		return false;
	}

	public void setActive(boolean isactive) {
		
	}

	public void setObjectPool(ObjectPool pool) {
		this.pool = pool;
	}

}
