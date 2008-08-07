package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

public class OracleClientConnectionFactory extends OracleConnectionFactory {

	@Override
	public OracleConnection newOracleConnectionInstance(SocketChannel channel,
			long createStamp) {
		return new OracleClientConnection(channel,createStamp);
	}

}
