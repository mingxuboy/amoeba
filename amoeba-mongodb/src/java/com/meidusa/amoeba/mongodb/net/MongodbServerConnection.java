package com.meidusa.amoeba.mongodb.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;

public class MongodbServerConnection extends AbstractMongodbConnection implements PoolableObject {
	private ObjectPool objectPool;
	
	private boolean active;
	public MongodbServerConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	@Override
	protected PacketInputStream createPacketInputStream() {
		return new MongodbFramedInputStream(true);
	}

	@Override
	protected PacketOutputStream createPakcetOutputStream() {
		return new MongodbFramingOutputStream(true);
	}

	public boolean checkIdle(long now){
		if (isClosed()) {
			return true;
		}
		return false;
	}
	
	@Override
	public ObjectPool getObjectPool() {
		return objectPool;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isRemovedFromPool() {
		return objectPool == null;
	}

	@Override
	public void setActive(boolean isactive) {
		this.active = isactive;
	}

	@Override
	public void setObjectPool(ObjectPool pool) {
		this.objectPool = pool;
	}

}
