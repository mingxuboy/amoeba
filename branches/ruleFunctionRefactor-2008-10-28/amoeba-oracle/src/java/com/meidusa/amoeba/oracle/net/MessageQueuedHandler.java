package com.meidusa.amoeba.oracle.net;

import com.meidusa.amoeba.net.MessageHandler;

/**
 * @author struct
 * @param <V>
 */
public interface MessageQueuedHandler<V> extends MessageHandler {

    public void push(OracleConnection conn, V x);

    public V pop(OracleConnection conn);

    public boolean inHandleProcess(OracleConnection conn);

    public void setInHandleProcess(OracleConnection conn, boolean inProcess);
}
