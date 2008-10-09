package com.meidusa.amoeba.oracle.net;

import com.meidusa.amoeba.net.MessageHandler;

/**
 * @author struct
 * @param <V>
 */
public interface MessageQueuedHandler<V> extends MessageHandler {

    public void push(OracleServerConnection conn, V x);

    public V pop(OracleServerConnection conn);

    public boolean inHandleProcess(OracleServerConnection conn);

    public void setInHandleProcess(OracleServerConnection conn, boolean inProcess);
}
