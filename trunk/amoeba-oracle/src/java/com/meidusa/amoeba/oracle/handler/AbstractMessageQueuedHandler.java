package com.meidusa.amoeba.oracle.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.oracle.net.MessageQueuedHandler;
import com.meidusa.amoeba.oracle.net.OracleServerConnection;
import com.meidusa.amoeba.util.Tuple;

public abstract class AbstractMessageQueuedHandler<V> implements
		MessageQueuedHandler<V> {
	
	protected Map<OracleServerConnection, Tuple<Boolean,BlockingQueue<V>>> exchangerMap = new HashMap<OracleServerConnection, Tuple<Boolean,BlockingQueue<V>>>();
	
	public void push(OracleServerConnection conn,V x){
		Tuple<Boolean,BlockingQueue<V>> tuple = getTuple(conn);
		try {
			tuple.right.put(x);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public V pop(OracleServerConnection conn){
		Tuple<Boolean,BlockingQueue<V>> tuple = getTuple(conn);
		try {
			return tuple.right.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean inHandleProcess(OracleServerConnection conn){
		Tuple<Boolean,BlockingQueue<V>> tuple = getTuple(conn);
		return tuple.left;
	}
	
	
	public final void handleMessage(Connection conn, byte[] message){
		if(conn instanceof OracleServerConnection){
			OracleServerConnection oconn = (OracleServerConnection) conn;
			Tuple<Boolean,BlockingQueue<V>> tuple = getTuple(oconn);
			try{
				synchronized (oconn.processLock){
					tuple.left = true;
					oconn.processLock.notifyAll();
				}
				doHandleMessage(oconn,message);
			}finally{
				synchronized (oconn.processLock){
					tuple.left = false;
					oconn.processLock.notifyAll();
				}
			}
		}else{
			doHandleMessage(conn,message);
		}
	}
	
	private Tuple<Boolean,BlockingQueue<V>> getTuple(OracleServerConnection conn){
		Tuple<Boolean,BlockingQueue<V>> tuple = exchangerMap.get(conn);
		if(tuple == null){
			synchronized (exchangerMap) {
				tuple = exchangerMap.get(conn);
				if(tuple == null){
					tuple = new Tuple<Boolean,BlockingQueue<V>>(false,new LinkedBlockingQueue<V>());
					exchangerMap.put(conn, tuple);
				}
			}
		}else{
			synchronized (tuple) {
				if(tuple.right == null){
					tuple.right = new LinkedBlockingQueue<V>();
				}
			}
		}
		return tuple;
	}
	
	public abstract void doHandleMessage(Connection conn, byte[] message);
}
