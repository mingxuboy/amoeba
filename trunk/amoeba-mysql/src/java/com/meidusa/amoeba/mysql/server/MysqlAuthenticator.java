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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.packet.AuthenticationPacket;
import com.meidusa.amoeba.mysql.util.CharsetMapping;
import com.meidusa.amoeba.mysql.util.Security;
import com.meidusa.amoeba.net.AuthResponseData;
import com.meidusa.amoeba.net.AuthingableConnection;
import com.meidusa.amoeba.server.DummyAuthenticator;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class MysqlAuthenticator extends DummyAuthenticator {
	protected static Logger logger = Logger.getLogger(MysqlAuthenticator.class);
	public MysqlAuthenticator() {
		
	}

	protected void processAuthentication(AuthingableConnection conn,
			AuthResponseData rdata) {
		MysqlClientConnection mysqlConn = (MysqlClientConnection)conn;
		logger.info("Accepting request: conn=" + conn);
		if(logger.isDebugEnabled()){
			StringBuffer buffer = new StringBuffer();
			for(byte byt : mysqlConn.getAuthenticationMessage()){
				buffer.append((char)byt);
			}
			logger.debug(buffer.toString());
		}
		String errorMessage = "";

		try{
			AuthenticationPacket autheticationPacket = new AuthenticationPacket();
			autheticationPacket.init(mysqlConn.getAuthenticationMessage());
			mysqlConn.setCharset(CharsetMapping.INDEX_TO_CHARSET[autheticationPacket.charsetNumber & 0xff]);
			boolean passwordchecked = false;
			

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
				rdata.message = "Access denied for user '"+autheticationPacket.user+"'@'"+ conn.getChannel().socket().getLocalAddress().getHostName() +"'"
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
