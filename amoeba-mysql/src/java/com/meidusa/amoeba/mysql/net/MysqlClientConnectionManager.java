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

import com.meidusa.amoeba.mysql.context.MysqlProxyRuntimeContext;
import com.meidusa.amoeba.mysql.handler.MySqlCommandDispatcher;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.HandshakePacket;
import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.net.AuthResponseData;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ServerableConnectionManager;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class MysqlClientConnectionManager extends ServerableConnectionManager{
	private final static String SERVER_VERSION = "5.1.22-mysql-community-amoeba-proxy";
	
	
	private static byte[] authenticateOkPacketData;
	protected HandshakePacket handshake;
	
	public MysqlClientConnectionManager() throws IOException {
	}
	
	public MysqlClientConnectionManager(String managerName,int port) throws IOException {
		super(managerName,port);
	}
	
	public MysqlClientConnectionManager(String name,String ipAddress,int port) throws IOException{
		super(name,ipAddress,port);
	}
	
	protected void willStart() {
		super.willStart();
		handshake = new HandshakePacket();
		handshake.packetId = 0;
		handshake.protocolVersion = 0x0a;//协议版本10
		handshake.seed = StringUtil.getRandomString(8);
		handshake.restOfScrambleBuff = StringUtil.getRandomString(12);
		
		handshake.serverStatus = 2;
		handshake.serverVersion = SERVER_VERSION;
		handshake.serverCapabilities = 41516;
	}

	/**
	 * 发送服务器端信息，跟用于密码加密的随机字符串
	 */
	protected void beforeAuthing(final Connection authing) {
		HandshakePacket handshakePacket = (HandshakePacket)handshake.clone();
		MysqlProxyRuntimeContext context = ((MysqlProxyRuntimeContext)MysqlProxyRuntimeContext.getInstance());
		handshakePacket.serverCharsetIndex =(byte)(context.getServerCharsetIndex() & 0xff);
		handshakePacket.threadId = Thread.currentThread().hashCode();
		handshakePacket.seed = StringUtil.getRandomString(8);
		handshakePacket.restOfScrambleBuff = StringUtil.getRandomString(12);
		MysqlClientConnection aconn = (MysqlClientConnection) authing;
		aconn.setSeed(handshakePacket.seed + handshakePacket.restOfScrambleBuff);
		aconn.postMessage(handshakePacket.toByteBuffer(authing).array());
	}
	
	public void connectionAuthenticateSuccess(Connection conn,AuthResponseData data) {
		super.connectionAuthenticateSuccess(conn,data);
		if(authenticateOkPacketData == null){
			OkPacket ok = new OkPacket();
			ok.packetId = 2;
			ok.affectedRows = 0;
			ok.insertId = 0;
			ok.serverStatus = 2;
			ok.warningCount = 0;
			authenticateOkPacketData = ok.toByteBuffer(conn).array();
		}
		conn.setMessageHandler(new MySqlCommandDispatcher());
		conn.postMessage(authenticateOkPacketData);
	}
	
	protected void connectionAuthenticateFaild(final Connection conn,AuthResponseData data) {
		super.connectionAuthenticateFaild(conn,data);
		ErrorPacket error = new ErrorPacket();
		error.resultPacketType = ErrorPacket.PACKET_TYPE_ERROR;
		error.packetId = 2;
		error.serverErrorMessage = data.message;
		error.sqlstate = "42S02";
		error.errno = 1000;
		conn.postMessage(error.toByteBuffer(conn).array());
	}

}
