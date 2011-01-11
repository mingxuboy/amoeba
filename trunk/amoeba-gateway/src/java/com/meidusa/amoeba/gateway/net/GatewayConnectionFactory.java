package com.meidusa.amoeba.gateway.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.AbstractConnectionFactory;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;

/**
 * 
 * @author struct
 *
 */
public class GatewayConnectionFactory extends AbstractConnectionFactory {
	private MessageHandler messageHandler;
	public MessageHandler getMessageHandler() {
		return messageHandler;
	}
	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	@Override
	protected Connection newConnectionInstance(SocketChannel channel,
			long createStamp) {
		Connection conn = new GatewayClientConnection(channel, createStamp);
		conn.setMessageHandler(messageHandler);
		return conn;
	}

}
