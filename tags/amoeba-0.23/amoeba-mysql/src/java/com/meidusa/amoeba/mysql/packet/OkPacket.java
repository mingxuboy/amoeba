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
package com.meidusa.amoeba.mysql.packet;

import java.io.UnsupportedEncodingException;
/**
 * From server to client in response to command, if no error and no result set. 
 * <pre>
 * VERSION 4.0
 *  Bytes                       Name
 *  -----                       ----
 *  1   (Length Coded Binary)   field_count, always = 0
 *  1-9 (Length Coded Binary)   affected_rows
 *  1-9 (Length Coded Binary)   insert_id
 *  2                           server_status
 *  n   (until end of packet)   message
 *  
 *  VERSION 4.1
 *  Bytes                       Name
 *  -----                       ----
 *  1   (Length Coded Binary)   field_count, always = 0
 *  1-9 (Length Coded Binary)   affected_rows
 *  1-9 (Length Coded Binary)   insert_id
 *  2                           server_status
 *  2                           warning_count
 *  n   (until end of packet)   message
 *  
 *  field_count:     always = 0
 *  
 *  affected_rows:   = number of rows affected by INSERT/UPDATE/DELETE
 *  
 *  insert_id:       If the statement generated any AUTO_INCREMENT number, 
 *                   it is returned here. Otherwise this field contains 0.
 *                   Note: when using for example a multiple row INSERT the
 *                   insert_id will be from the first row inserted, not from
 *                   last.
 *  
 *  server_status:   = The client can use this to check if the
 *                   command was inside a transaction.
 *  
 *  warning_count:   number of warnings
 *  
 *  message:         For example, after a multi-line INSERT, message might be
 *                   "Records: 3 Duplicates: 0 Warnings: 0"
 *=========================================================================       
 *</pre>
 *       
 *<pre>
 *  The message field is optional. 
 *  Alternative terms: OK Packet is also known as "okay packet" or "ok packet" or "OK-Packet". 
 *  field_count is also known as "number of rows" or "marker for ok packet". 
 *  message is also known as "Messagetext". 
 *  OK Packets (and result set packets) are also called "Result packets". 
 *</pre>
 *
 *<pre>
 *================================================================
 * Example OK Packet
 *                     Hexadecimal                ASCII
 *                     -----------                -----
 * field_count         00                         .
 * affected_rows       01                         .
 * insert_id           00                         .
 * server_status       02 00                      ..
 * warning_count       00 00                      ..
 *</pre>
 *
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class OkPacket extends AbstractResultPacket {
	public long affectedRows;
	public long insertId;
	public int serverStatus;
	public int warningCount;
	public String message;
	
	public void init(PacketBuffer buffer) {
		super.init(buffer);
		
		affectedRows = buffer.readFieldLength();
		insertId = buffer.readFieldLength();
		serverStatus = buffer.readInt();
		warningCount = buffer.readInt();
		
		if(buffer.getPosition()<buffer.getBufLength()){
			message	= buffer.readString();
		}
	}

	public void write2Buffer(PacketBuffer buffer) throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeFieldLength(affectedRows);
		buffer.writeFieldLength(insertId);
		buffer.writeInt(serverStatus);
		buffer.writeInt(warningCount);
		if(message != null){
			buffer.writeString(message);
		}
	}
	
	protected int calculatePacketSize(){
		int packLength = super.calculatePacketSize();
		packLength += 4+4+2+2+(message == null?0:message.length()) * 2;
		return packLength;
	}
}
