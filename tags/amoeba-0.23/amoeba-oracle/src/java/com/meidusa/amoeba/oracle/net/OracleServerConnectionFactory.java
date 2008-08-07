package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

public class OracleServerConnectionFactory extends OracleConnectionFactory {

	@Override
	public OracleConnection newOracleConnectionInstance(SocketChannel channel,
			long createStamp) {
		return new OracleServerConnection(channel,createStamp);
	}

}
