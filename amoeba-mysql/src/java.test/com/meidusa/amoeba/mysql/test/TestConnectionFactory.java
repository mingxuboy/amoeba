package com.meidusa.amoeba.mysql.test;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.AbstractConnectionFactory;
import com.meidusa.amoeba.net.Connection;

public class TestConnectionFactory extends AbstractConnectionFactory {

	@Override
	protected Connection newConnectionInstance(SocketChannel channel,
			long createStamp) {
		return new TestConnection(channel,createStamp);
	}

}
