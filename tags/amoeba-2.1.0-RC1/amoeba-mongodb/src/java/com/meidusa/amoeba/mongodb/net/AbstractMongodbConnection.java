/*
 * Copyright amoeba.meidusa.com
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mongodb.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;

public abstract class AbstractMongodbConnection extends Connection {
	protected SessionMessageHandler sessionMessageHandler = null;
	
	public AbstractMongodbConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	public SessionMessageHandler getSessionMessageHandler() {
		return sessionMessageHandler;
	}

	public void setSessionMessageHandler(SessionMessageHandler singleHandler) {
		this.sessionMessageHandler = singleHandler;
	}
	
    protected abstract void doReceiveMessage(byte[] message);
    
    protected void messageProcess() {
		//_handler.handleMessage(this);
    }
    
	public void postMessage(byte[] msg) {
		postMessage(ByteBuffer.wrap(msg));
	}
	
	@Override
	protected PacketInputStream createPacketInputStream() {
		return new MongodbFramedInputStream(true);
	}

	@Override
	protected PacketOutputStream createPacketOutputStream() {
		return new MongodbFramingOutputStream(true);
	}
	
	public boolean checkIdle(long now){
		if (isClosed()) {
			return true;
		}else{
			SessionMessageHandler sessionMessageHandler = this.sessionMessageHandler;
			if(sessionMessageHandler != null){
				return sessionMessageHandler.checkIdle(now);
			}
		}
		return false;
	}
}
