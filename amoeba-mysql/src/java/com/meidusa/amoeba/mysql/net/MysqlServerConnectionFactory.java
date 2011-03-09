/*
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
package com.meidusa.amoeba.mysql.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.mysql.net.packet.MysqlPingPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.PoolableConnectionFactory;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class MysqlServerConnectionFactory extends PoolableConnectionFactory{
	private boolean connPing = false;
	

	public boolean isConnPing() {
		return connPing;
	}

	public void setConnPing(boolean connPing) {
		this.connPing = connPing;
	}

	@Override
	protected Connection newConnectionInstance(SocketChannel channel,
			long createStamp) {
		return new MysqlServerConnection(channel,createStamp);
	}
	
	public boolean validateObject(Object arg0) {
		boolean isValid = super.validateObject(arg0);
		if(isValid){
			if(connPing){
				MysqlServerConnection conn = (MysqlServerConnection)arg0;
				
				MessageHandler handler = conn.getMessageHandler();
				try{
					synchronized (handler) {
						PingPacketHandler pingHandler = new PingPacketHandler(handler);
						conn.setMessageHandler(pingHandler);
						conn.postMessage(new MysqlPingPacket().toByteBuffer(conn));
						try {
							handler.wait(2*1000);
						} catch (InterruptedException e) {
						}
						if(pingHandler.msgReturn){
							return true;
						}else{
							return false;
						}
					}
				}finally{
					conn.setMessageHandler(handler);
				}
			}else{
				return true;
			}
		}else{
			return false;
		}
	}
	
	class PingPacketHandler implements MessageHandler{
		private MessageHandler handler;
		private boolean msgReturn = false;
		PingPacketHandler(MessageHandler handler){
			this.handler = handler;
		}
		@Override
		public void handleMessage(Connection conn) {
			byte[] msg = conn.getInQueue().get();
			if(msg != null){
				msgReturn = true;
			}
			synchronized (handler) {
				handler.notifyAll();
			}
		}
		
	}
}
