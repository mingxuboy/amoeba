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
 * From server to client, for prepared statements which contain parameters. 
 * The Parameter Packets follow a Prepared Statement Initialization Packet which has a positive value in the parameters field.
 * <pre>
 * Bytes                   Name
 *  -----                   ----
 *  2                       type
 *  2                       flags
 *  1                       decimals
 *  4                       length
 *  
 *  type:                Same as for type field in a Field Packet.
 *  
 *  flags:               Same as for flags field in a Field Packet.
 *  
 *  decimals:            Same as for decimals field in a Field Packet.
 *  
 *  length:              Same as for length field in a Field Packet.
 *  </pre>
 *  
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class ParameterPacket extends AbstractPacket {
	
	public int type;
	public int flags;
	public byte decimals;
	public long length;
	public void init(MysqlPacketBuffer buffer) {
		super.init(buffer);
		type = buffer.readByte();
		flags = buffer.readInt();
		decimals = buffer.readByte();
		length = buffer.readLong();
	}
	
	protected void write2Buffer(MysqlPacketBuffer buffer) throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(type);
		buffer.writeInt(flags);
		buffer.writeByte(decimals);
		buffer.writeLong(length);
	}
	
	protected int calculatePacketSize(){
		int packLength = super.calculatePacketSize();
		packLength += 1+2+1+4;
		return packLength;
	}

}
