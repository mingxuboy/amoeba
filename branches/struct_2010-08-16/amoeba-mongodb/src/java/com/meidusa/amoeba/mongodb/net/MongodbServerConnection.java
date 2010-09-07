package com.meidusa.amoeba.mongodb.net;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;

public class MongodbServerConnection extends AbstractMongodbConnection implements PoolableObject,MessageHandler {
	private static Logger logger = Logger.getLogger(MongodbServerConnection.class);
	
	private ObjectPool objectPool;
	private SessionMessageHandler sessionMessageHandler = null;
	private boolean active;
	public MongodbServerConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
		this.setMessageHandler(this);
	}

	
	public SessionMessageHandler getSessionMessageHandler() {
		return sessionMessageHandler;
	}

	public void setSessionMessageHandler(SessionMessageHandler singleHandler) {
		this.sessionMessageHandler = singleHandler;
	}

	/*protected void messageProcess() {
		byte[] message = null;
		while((message = getInQueue().getNonBlocking()) != null){
			sessionMessageHandler.handleMessage(this,message);
		}
    }*/
	
	protected void doReceiveMessage(byte[] message){
		sessionMessageHandler.handleMessage(this,message);
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


	
	@Override
	public void handleMessage(Connection conn) {
		logger.error("raw message handler");
	}

}
