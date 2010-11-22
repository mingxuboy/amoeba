package com.meidusa.amoeba.context;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;

public class RuntimeContext implements Initialisable {
	private String serverCharset;
	private Executor readExecutor;
	private Executor clientSideExecutor;
	private Executor serverSideExecutor;
	private int readThreadPoolSize = 16;
	private int clientSideThreadPoolSize = 16;
	private int serverSideThreadPoolSize = 16;
	
	/**
	 * query time out
	 */
	private int queryTimeout;
	private boolean useMultipleThread = true;
	
	/**
	 * max result --Overload protection , query session was killed , results returned size exceed
	 */
	private int maxResult = -1;
	
	public int getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	public boolean isUseMultipleThread() {
		return useMultipleThread;
	}

	public void setUseMultipleThread(boolean useMultipleThread) {
		this.useMultipleThread = useMultipleThread;
	}

	public String getServerCharset() {
		return serverCharset;
	}

	public Executor getReadExecutor() {
		return readExecutor;
	}

	public void setReadExecutor(Executor readExecutor) {
		this.readExecutor = readExecutor;
	}

	public Executor getClientSideExecutor() {
		return clientSideExecutor;
	}

	public void setClientSideExecutor(Executor clientSideExecutor) {
		this.clientSideExecutor = clientSideExecutor;
	}

	public Executor getServerSideExecutor() {
		return serverSideExecutor;
	}

	public void setServerSideExecutor(Executor serverSideExecutor) {
		this.serverSideExecutor = serverSideExecutor;
	}

	public int getReadThreadPoolSize() {
		return readThreadPoolSize;
	}

	public void setReadThreadPoolSize(int readThreadPoolSize) {
		this.readThreadPoolSize = readThreadPoolSize;
	}

	public int getClientSideThreadPoolSize() {
		return clientSideThreadPoolSize;
	}

	public void setClientSideThreadPoolSize(int clientSideThreadPoolSize) {
		this.clientSideThreadPoolSize = clientSideThreadPoolSize;
	}

	public int getServerSideThreadPoolSize() {
		return serverSideThreadPoolSize;
	}

	public void setServerSideThreadPoolSize(int serverSideThreadPoolSize) {
		this.serverSideThreadPoolSize = serverSideThreadPoolSize;
	}

	public void setServerCharset(String serverCharset) {
		this.serverCharset = serverCharset;
	}

	public int getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(int maxResult) {
		this.maxResult = maxResult;
	}

	static class ReNameableThreadExecutor extends ThreadPoolExecutor {

		public ReNameableThreadExecutor(int poolSize) {
			super(poolSize, poolSize, Long.MAX_VALUE, TimeUnit.NANOSECONDS,
					new LinkedBlockingQueue<Runnable>());
		}
	}

	@Override
	public void init() throws InitialisationException {
		readExecutor = new ReNameableThreadExecutor(getReadThreadPoolSize());
		serverSideExecutor = new ReNameableThreadExecutor(
				getServerSideThreadPoolSize());
		clientSideExecutor = new ReNameableThreadExecutor(
				getClientSideThreadPoolSize());
	}
}
