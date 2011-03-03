package com.meidusa.amoeba.aladdin.net;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.aladdin.handler.AladdinMessageDispatcher;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.net.AuthResponseData;

public class AladdinClientConnection extends MysqlClientConnection {
	private static Logger logger = Logger.getLogger(AladdinClientConnection.class);
	public AladdinClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

/*	public void setSchema(String schema) {
		//ignore client mysql schema
		//	this.schema = schema;
	}*/
	
    protected void connectionAuthenticateSuccess(AuthResponseData data) {
        if (logger.isInfoEnabled()) {
            logger.info("Connection Authenticate success [ conn=" + this + "].");
        }
        
        setMessageHandler(new AladdinMessageDispatcher());
        postMessage(AUTHENTICATEOKPACKETDATA);
        this.afterAuth();
   }
}
