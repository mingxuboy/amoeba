package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.PoolableConnectionFactory;

public class OracleServerConnectionFactory extends PoolableConnectionFactory {

    @Override
    public OracleConnection newConnectionInstance(SocketChannel channel, long createStamp) {
        return new OracleServerConnection(channel, createStamp);
    }

}
