package com.meidusa.amoeba.oracle.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.net.MessageQueuedHandler;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.net.OracleServerConnection;
import com.meidusa.amoeba.util.Tuple;

public abstract class AbstractMessageQueuedHandler<V> implements MessageQueuedHandler<V>, Sessionable {

    private static Logger                                                   logger       = Logger.getLogger(AbstractMessageQueuedHandler.class);

    protected Map<OracleConnection, Tuple<Boolean, BlockingQueue<V>>> exchangerMap = new HashMap<OracleConnection, Tuple<Boolean, BlockingQueue<V>>>();

    public void push(OracleConnection conn, V x) {
        Tuple<Boolean, BlockingQueue<V>> tuple = getTuple(conn);
        try {
            tuple.right.put(x);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public V pop(OracleConnection conn) {
        Tuple<Boolean, BlockingQueue<V>> tuple = getTuple(conn);
        try {
            return tuple.right.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean inHandleProcess(OracleConnection conn) {
        Tuple<Boolean, BlockingQueue<V>> tuple = getTuple(conn);
        return tuple.left;
    }

    public void setInHandleProcess(OracleConnection conn, boolean inProcess) {
        Tuple<Boolean, BlockingQueue<V>> tuple = getTuple(conn);
        synchronized (conn.processLock) {
            tuple.left = inProcess;
        }
    }

    public final void handleMessage(Connection conn, byte[] message) {
        if (conn instanceof OracleServerConnection) {
            OracleServerConnection oconn = (OracleServerConnection) conn;
            try {
                doHandleMessage(oconn, message);
            } catch (Exception e) {
                logger.error("doHandleMessage Exception", e);
                endSession();
            } finally {
                setInHandleProcess(oconn, false);
            }
        } else {
            try {
                doHandleMessage(conn, message);
            } catch (Exception e) {
                logger.error("doHandleMessage Exception", e);
                endSession();
            }
        }
    }

    private Tuple<Boolean, BlockingQueue<V>> getTuple(OracleConnection conn) {
        Tuple<Boolean, BlockingQueue<V>> tuple = exchangerMap.get(conn);
        if (tuple == null) {
            synchronized (exchangerMap) {
                tuple = exchangerMap.get(conn);
                if (tuple == null) {
                    tuple = new Tuple<Boolean, BlockingQueue<V>>(false, new LinkedBlockingQueue<V>());
                    exchangerMap.put(conn, tuple);
                }
            }
        } else {
            synchronized (tuple) {
                if (tuple.right == null) {
                    tuple.right = new LinkedBlockingQueue<V>();
                }
            }
        }
        return tuple;
    }

    public abstract void doHandleMessage(Connection conn, byte[] message);
}
