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
 * A "result packet" is a packet that goes from the server to the client in response to a Client Authentication Packet or Command Packet. 
 * To distinguish between the types of result packets,
 *  a client must look at the first byte in the packet. 
 *  We will call this byte "field_count" in the description of each individual package, 
 *  although it goes by several names.
 *<pre>  
 * Type Of Result Packet       Hexadecimal Value Of First Byte (field_count)
 *  ---------------------       ---------------------------------------------
 *  
 *  OK Packet                   00
 *  Error Packet                ff
 *  Result Set Packet           1-250 (first byte of Length-Coded Binary)
 *  Field Packet                1-250 ("")
 *  Row Data Packet             1-250 ("")
 *  EOF Packet                  fe
 *  </pre>
 *  
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class AbstractResultPacket extends AbstractPacket {
	public static final byte PACKET_TYPE_OK= 0x00;
	public static final byte PACKET_TYPE_ERROR= (byte)0xff;
	public static final byte PACKET_TYPE_EOF= (byte)0xfe;
	
	public byte resultPacketType;
	
	public void init(PacketBuffer buffer) {
		super.init(buffer);
		resultPacketType = buffer.readByte();
	}

	protected void write2Buffer(PacketBuffer buffer) throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeByte(resultPacketType);
	}
	
	protected int calculatePacketSize(){
		int packLength = super.calculatePacketSize();
        packLength += 1;
		return packLength;
	}
}
