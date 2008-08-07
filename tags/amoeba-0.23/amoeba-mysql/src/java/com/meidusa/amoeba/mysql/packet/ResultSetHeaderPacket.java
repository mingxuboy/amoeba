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
public class ResultSetHeaderPacket extends AbstractPacket {
	public long columns;
	public long extra;
	public void init(PacketBuffer buffer){
		super.init(buffer);
		columns = buffer.readFieldLength();
		if(buffer.getBufLength()> buffer.getPosition()){
			extra = buffer.readFieldLength();
		}
	}
	
	public void write2Buffer(PacketBuffer buffer) throws UnsupportedEncodingException{
		super.write2Buffer(buffer);
		buffer.writeFieldLength(columns);
		if(extra >0){
			buffer.writeFieldLength(extra);
		}
	}

	protected int calculatePacketSize(){
		int packLength = super.calculatePacketSize();
		packLength += 4;
		return packLength;
	}
	
}
