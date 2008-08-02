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
package com.meidusa.amoeba.mysql.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.poolable.PoolableConnectionFactory;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.net.SocketChannelFactory;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class MysqlServerConnectionFactory extends PoolableConnectionFactory implements Initialisable{
	private static Logger logger = Logger.getLogger(MysqlServerConnectionFactory.class);
	private String manager;
	private int port;
	private String ipAddress;
	private String user;
	private String password;
	private String schema;
	
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public MysqlServerConnectionFactory(){
	}
	
	public Connection createConnection(SocketChannel channel,
			long createStamp) {
		MysqlServerConnection conn = new MysqlServerConnection(channel,createStamp);
		conn.setSchema(this.getSchema());
		if(!StringUtil.isEmpty(user)){
			conn.setUser(user);
			conn.setPassword(password);
		}else{
			conn.setUser(ProxyRuntimeContext.getInstance().getConfig().getUser());
			conn.setPassword(ProxyRuntimeContext.getInstance().getConfig().getPassword());
		}
		
		return conn;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void init() throws InitialisationException {
		this.setSocketChannelFactory(new SocketChannelFactory(){

			public SocketChannel createSokectChannel() throws IOException {
				SocketChannel socketChannel = null;
				try{
					if(ipAddress == null){
						socketChannel = SocketChannel.open(new InetSocketAddress(port));
					}else{
						socketChannel = SocketChannel.open(new InetSocketAddress(ipAddress, port));
					}
					socketChannel.configureBlocking(false);
				}catch(IOException e){
					logger.error("could not connect to server["+ipAddress+":"+port+"]",e);
					throw e;
				}
				return socketChannel;
			}
		});
		
		ConnectionManager conMgr = ProxyRuntimeContext.getInstance().getConnectionManagerList().get(manager);
		if(conMgr == null){
			throw new InitialisationException("can not found connectionManager by name="+manager);
		}
		this.setConnectionManager(conMgr);
		
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
