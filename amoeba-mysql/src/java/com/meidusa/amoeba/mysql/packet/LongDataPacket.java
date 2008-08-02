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
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class LongDataPacket extends AbstractPacket {
	public byte code;
	public long statementId;
	public int parameterIndex;
	public int type;
	public byte[] data;
	
	public void init(PacketBuffer buffer) {
		super.init(buffer);
		code = buffer.readByte();
		statementId = buffer.readLong();
		parameterIndex = buffer.readInt();
		type = buffer.readInt();
		data = buffer.getBytes(buffer.getPosition(), buffer.getBufLength() - buffer.getPosition());
	}
	
	protected void write2Buffer(PacketBuffer buffer) throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeByte(code);
		buffer.writeLong(statementId);
		buffer.writeInt(parameterIndex);
		buffer.writeInt(type);
		buffer.writeBytesNoNull(data);
	}
	
}
