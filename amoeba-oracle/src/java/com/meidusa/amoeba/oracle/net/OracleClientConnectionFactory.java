package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.FrontendConnectionFactory;

public class OracleClientConnectionFactory extends FrontendConnectionFactory {

    @Override
    protected OracleConnection newConnectionInstance(SocketChannel channel, long createStamp) {
        return new OracleClientConnection(channel, createStamp);
    }

}
