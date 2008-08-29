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
package com.meidusa.amoeba.mysql.net.packet;

import java.io.UnsupportedEncodingException;

/**
 * From client to server during initial handshake. 
 * 
 * <pre>
 * 	
 *  <b>VERSION 4.0</b>
 *  Bytes                        Name
 *  -----                        ----
 *  2                            client_flags
 *  3                            max_packet_size
 *  n  (Null-Terminated String)  user
 *  8                            scramble_buff
 *  1                            (filler) always 0x00
 *  
 *  <b>VERSION 4.1</b>
 *  Bytes                        Name
 *  -----                        ----
 *  4                            client_flags
 *  4                            max_packet_size
 *  1                            charset_number
 *  23                           (filler) always 0x00...
 *  n (Null-Terminated String)   user
 *  n (Length Coded Binary)      scramble_buff (1 + x bytes)
 *  1                            (filler) always 0x00
 *  n (Null-Terminated String)   databasename
 *  
 *  client_flags:            CLIENT_xxx options. The list of possible flag
 *                           values is in the description of the Handshake
 *                           Initialisation Packet, for server_capabilities.
 *                           For some of the bits, the server passed "what
 *                           it's capable of". The client leaves some of the
 *                           bits on, adds others, and passes back to the server.
 *                           One important flag is: whether compression is desired.
 *  
 *  max_packet_size:         the maximum number of bytes in a packet for the client
 *  
 *  charset_number:          in the same domain as the server_language field that
 *                           the server passes in the Handshake Initialization packet.
 *  
 *  user:                    identification
 *  
 *  scramble_buff:           the password, after encrypting using the scramble_buff
 *                           contents passed by the server (see "Password functions"
 *                           section elsewhere in this document)
 *                           if length is zero, no password was given
 *  
 *  databasename:            name of schema to use initially
 *  
 *  </pre>
 * 该数据包只支持mysql 4.1版本以后,mysql协议版本10，并且是非安全连接
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class AuthenticationPacket extends AbstractPacket{
	public long clientParam;
	public long maxThreeBytes;
	
	// charset, JDBC will connect as 'latin1',
    // and use 'SET NAMES' to change to the desired
    // charset after the connection is established.
    public byte charsetNumber = 8;
    
	public String user;
    
	//16字节
	public byte[] encryptedPassword;
	
	public String database;

	public void init(MysqlPacketBuffer buffer) {
		super.init(buffer);
		clientParam 	= buffer.readLong();
		maxThreeBytes 	= buffer.readLong();
		charsetNumber	= buffer.readByte();
		//跳过23个填充字节
		buffer.setPosition(buffer.getPosition()+23);
		user			= buffer.readString(CODE_PAGE_1252);
		//buffer.read
		long passwordLength = buffer.readFieldLength();
		
		encryptedPassword	= buffer.getBytes(buffer.getPosition(),(int)passwordLength);
		//encryptedPassword = buffer.readString(CODE_PAGE_1252);
		
		buffer.setPosition(buffer.getPosition()+(int)passwordLength);
		if((clientParam & CLIENT_CONNECT_WITH_DB) != 0){
			if(buffer.getPosition() < buffer.getBufLength()){
				database		= buffer.readString(CODE_PAGE_1252);
			}
		}
		
		/*if(logger.isDebugEnabled()){
			StringBuilder builder = new StringBuilder();
			builder.append("\n");
			builder.append("==============================Client Flag===============================\n");
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
		}*/
	}

	public void write2Buffer(MysqlPacketBuffer buffer) throws UnsupportedEncodingException{
		super.write2Buffer(buffer);
		buffer.writeLong(clientParam);
		buffer.writeLong(maxThreeBytes);
		buffer.writeByte(charsetNumber);
		buffer.writeBytesNoNull(new byte[23]);
		
		if(user == null){
			user = "";
		}
		buffer.writeString(user, CODE_PAGE_1252);
		
		/*if(encryptedPassword == null){
			encryptedPassword = "";
		}*/
		//buffer.writeString(encryptedPassword, CODE_PAGE_1252);
		if(encryptedPassword != null && encryptedPassword.length != 0){
			buffer.writeFieldLength(encryptedPassword.length);
			buffer.writeBytesNoNull(encryptedPassword);
		}else{
			buffer.writeByte((byte)0);
		}
		
		if((clientParam & CLIENT_CONNECT_WITH_DB) != 0){
			if(database != null){
				buffer.writeString(database, CODE_PAGE_1252);
			}
		}
		
	}
	
	protected int calculatePacketSize(){
		int packLength = super.calculatePacketSize();
		int passwordLength = 16;
        int userLength = (user != null) ? user.length() : 0;
        int databaseLength = (database != null) ? database.length() : 0;
        packLength += ((userLength + passwordLength + databaseLength) * 2) + 7 + HEADER_SIZE + AUTH_411_OVERHEAD;
		return packLength;
	}

}
