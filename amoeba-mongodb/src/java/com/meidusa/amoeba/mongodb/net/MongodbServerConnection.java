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

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;

public class MongodbServerConnection extends AbstractMongodbConnection implements PoolableObject,MessageHandler {
	private static Logger logger = Logger.getLogger(MongodbServerConnection.class);
	
	private ObjectPool objectPool;
	
	private boolean active;
	public MongodbServerConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
		this.setMessageHandler(this);
	}

	/*protected void messageProcess() {
		byte[] message = null;
		while((message = getInQueue().getNonBlocking()) != null){
			sessionMessageHandler.handleMessage(this,message);
		}
    }*/
	
	protected void doReceiveMessage(byte[] message){
		sessionMessageHandler.handleMessage(this,message);
	}
	
	@Override
	public ObjectPool getObjectPool() {
		return objectPool;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isRemovedFromPool() {
		return objectPool == null;
	}

	@Override
	public void setActive(boolean isactive) {
		this.active = isactive;
	}

	@Override
	public void setObjectPool(ObjectPool pool) {
		this.objectPool = pool;
	}


	
	@Override
	public void handleMessage(Connection conn) {
		logger.error("raw message handler");
	}

}
