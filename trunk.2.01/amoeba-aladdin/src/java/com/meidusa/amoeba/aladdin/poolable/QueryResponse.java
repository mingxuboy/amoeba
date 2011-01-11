package com.meidusa.amoeba.aladdin.poolable;

import com.meidusa.amoeba.aladdin.handler.MessageHandlerRunner;
import com.meidusa.amoeba.aladdin.handler.MessageHandlerRunnerProvider;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;

public class QueryResponse implements PoolableObject,MessageHandlerRunnerProvider{
	private ObjectPool pool;
	private boolean active;
	private MessageHandlerRunner runner; 
	public ObjectPool getObjectPool() {
		return pool;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isRemovedFromPool() {
		return pool == null;
	}

	public void setActive(boolean isactive) {
		this.active = isactive;
	}

	public void setObjectPool(ObjectPool pool) {
		this.pool = pool;
	}

	public void setMessageHandlerRunner(MessageHandlerRunner runner){
		this.runner = runner;
	}
	
	public MessageHandlerRunner getRunner() {
		return runner;
	}

}
