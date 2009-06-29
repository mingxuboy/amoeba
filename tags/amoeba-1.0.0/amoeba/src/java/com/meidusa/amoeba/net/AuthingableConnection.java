/*
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
package com.meidusa.amoeba.net;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * 一个可表示是否通过验证的Connection
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class AuthingableConnection extends Connection implements MessageHandler{
	private static Logger logger = Logger.getLogger(AuthingableConnection.class);
	protected boolean authenticated;//是否验证通过
	private boolean authenticatedSeted = false;
	private String user;
	private String password;
	
	public AuthingableConnection(SocketChannel channel, long createStamp){
		super(channel, createStamp);
		setMessageHandler(this);
	}

	public boolean isAuthenticated(){
		return authenticated;
	}
	
	public boolean isAuthenticatedSeted() {
		return authenticatedSeted;
	}
	
	public void setAuthenticated(boolean authenticated){
		synchronized(this){
			authenticatedSeted = true;
			this.authenticated = authenticated;
			this.notifyAll();
			if(logger.isDebugEnabled()){
				try{
				logger.debug(this.toString()+" , authenticated: "+ authenticated +" (" + (this.getChannel()!= null?this.getChannel().socket().getInetAddress().toString():"")+")");
				}catch(Exception e){};
			}
		}
	}
	
	public boolean isAuthenticatedWithBlocked(long timeout){
		if(authenticatedSeted) return authenticated;
		synchronized(this){
			if(authenticatedSeted) return authenticated;
			try {
				this.wait(timeout);
			} catch (InterruptedException e) {
			}
		}
		
		if(!authenticatedSeted){
			logger.warn("authenticate to server:"+(this._channel!= null?this._channel.socket():"") +" time out");
		}
		return authenticated;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
