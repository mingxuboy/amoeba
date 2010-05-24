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
package com.meidusa.amoeba.mysql.server;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.mysql.io.MySqlPacketConstant;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.AuthenticationPacket;
import com.meidusa.amoeba.mysql.util.CharsetMapping;
import com.meidusa.amoeba.mysql.util.Security;
import com.meidusa.amoeba.net.AuthResponseData;
import com.meidusa.amoeba.net.AuthingableConnection;
import com.meidusa.amoeba.server.DummyAuthenticator;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
@SuppressWarnings("unchecked")
public class MysqlClientAuthenticator extends DummyAuthenticator implements MySqlPacketConstant{
	protected static Logger logger = Logger.getLogger(MysqlClientAuthenticator.class);
	private Map map = new LRUMap(100);
	public MysqlClientAuthenticator() {
		
	}

	protected void processAuthentication(AuthingableConnection conn,
			AuthResponseData rdata) {
		MysqlClientConnection mysqlConn = (MysqlClientConnection)conn;
		
		if(logger.isInfoEnabled()){
			logger.info("Accepting request: conn=" + conn);
		}
		String errorMessage = "";

		try{
			AuthenticationPacket autheticationPacket = new AuthenticationPacket();
			autheticationPacket.init(mysqlConn.getAuthenticationMessage(),conn);
			mysqlConn.setCharset(CharsetMapping.INDEX_TO_CHARSET[autheticationPacket.charsetNumber & 0xff]);
			boolean passwordchecked = false;
			if(logger.isDebugEnabled()){
				logger.debug("client charset="+CharsetMapping.INDEX_TO_CHARSET[autheticationPacket.charsetNumber & 0xff]);
				if(conn.getInetAddress() != null && map.get(conn.getInetAddress().getHostAddress()) == null){
					map.put(conn.getInetAddress().getHostAddress(), Boolean.TRUE);
					long clientParam = autheticationPacket.clientParam;
					StringBuilder builder = new StringBuilder();
					builder.append("\n");
					builder.append("===========").append(conn.getInetAddress().getHostAddress())
					.append("   Client Flag ==============\n");
					builder.append("CLIENT_LONG_PASSWORD:").append(((clientParam & CLIENT_LONG_PASSWORD)!=0)).append("\n");
					builder.append("CLIENT_FOUND_ROWS:").append(((clientParam & CLIENT_FOUND_ROWS)!=0)).append("\n");
					builder.append("CLIENT_LONG_FLAG:").append(((clientParam & CLIENT_LONG_FLAG)!=0)).append("\n");
					builder.append("CLIENT_CONNECT_WITH_DB:").append(((clientParam & CLIENT_CONNECT_WITH_DB)!=0)).append("\n");
					builder.append("CLIENT_NO_SCHEMA:").append(((clientParam & CLIENT_NO_SCHEMA)!=0)).append("\n");
					builder.append("CLIENT_COMPRESS:").append(((clientParam & CLIENT_COMPRESS)!=0)).append("\n");
					builder.append("CLIENT_ODBC:").append(((clientParam & CLIENT_ODBC)!=0)).append("\n");
					builder.append("CLIENT_LOCAL_FILES:").append(((clientParam & CLIENT_LOCAL_FILES)!=0)).append("\n");
					builder.append("CLIENT_IGNORE_SPACE:").append(((clientParam & CLIENT_IGNORE_SPACE)!=0)).append("\n");
					builder.append("CLIENT_PROTOCOL_41:").append(((clientParam & CLIENT_PROTOCOL_41)!=0)).append("\n");
					builder.append("CLIENT_INTERACTIVE:").append(((clientParam & CLIENT_INTERACTIVE)!=0)).append("\n");
					builder.append("CLIENT_SSL:").append(((clientParam & CLIENT_SSL)!=0)).append("\n");
					builder.append("CLIENT_IGNORE_SIGPIPE:").append(((clientParam & CLIENT_IGNORE_SIGPIPE)!=0)).append("\n");
					builder.append("CLIENT_TRANSACTIONS:").append(((clientParam & CLIENT_TRANSACTIONS)!=0)).append("\n");
					builder.append("CLIENT_RESERVED:").append(((clientParam & CLIENT_RESERVED)!=0)).append("\n");
					builder.append("CLIENT_SECURE_CONNECTION:").append(((clientParam & CLIENT_SECURE_CONNECTION)!=0)).append("\n");
					builder.append("CLIENT_MULTI_STATEMENTS:").append(((clientParam & CLIENT_MULTI_STATEMENTS)!=0)).append("\n");
					builder.append("CLIENT_MULTI_RESULTS:").append(((clientParam & CLIENT_MULTI_RESULTS)!=0)).append("\n");
					builder.append("===========================END Client Flag===============================\n");
					logger.debug(builder.toString());
				}
			}

			if(mysqlConn.getPassword() != null){
				String encryptPassword = new String(Security.scramble411(mysqlConn.getPassword(),mysqlConn.getSeed()),AuthenticationPacket.CODE_PAGE_1252);
			
				passwordchecked = StringUtils.equals(new String(autheticationPacket.encryptedPassword,AuthenticationPacket.CODE_PAGE_1252), encryptPassword);
			}else{
				if(autheticationPacket.encryptedPassword == null || autheticationPacket.encryptedPassword.length ==0){
					passwordchecked = true;
				}
			}
			if(StringUtil.equals(mysqlConn.getUser(),autheticationPacket.user) && passwordchecked){
				rdata.code = AuthResponseData.SUCCESS;
				if(logger.isDebugEnabled()){
					logger.debug(autheticationPacket.toString());
				}
			}else{
				rdata.code = AuthResponseData.ERROR;
				rdata.message = "Access denied for user '"+autheticationPacket.user+"'@'"+ conn.getSocketId() +"'"
				+(autheticationPacket.encryptedPassword !=null?"(using password: YES)":"");
			}
			
			mysqlConn.setSchema(autheticationPacket.database);
		} catch (UnsupportedEncodingException e) {
			errorMessage = e.getMessage();
			rdata.code = AuthResponseData.ERROR;
			rdata.message = errorMessage;
			logger.error("UnsupportedEncodingException error",e);
		} catch (NoSuchAlgorithmException e) {
			errorMessage = e.getMessage();
			rdata.code = AuthResponseData.ERROR;
			rdata.message = errorMessage;
			logger.error("NoSuchAlgorithmException error",e);
		}catch(Exception e){
			errorMessage = e.getMessage();
			logger.error("processAuthentication error",e);
			rdata.code = AuthResponseData.ERROR;
			rdata.message = errorMessage;
		}
		
	}
}
