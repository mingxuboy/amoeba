/*
 * Copyright amoeba.meidusa.com
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mongodb.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.mongodb.io.MongodbFramedInputStream;
import com.meidusa.amoeba.mongodb.io.MongodbFramingOutputStream;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;

public abstract class AbstractMongodbConnection extends Connection {
	
	
	public AbstractMongodbConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
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
}
