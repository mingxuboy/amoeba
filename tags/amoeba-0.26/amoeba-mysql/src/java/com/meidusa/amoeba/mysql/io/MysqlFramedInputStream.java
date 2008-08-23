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
package com.meidusa.amoeba.mysql.io;

import com.meidusa.amoeba.net.io.PacketInputStream;

/**
 * 
 * <b> The Packet Header </b>
 * 
 * <pre>
 * Bytes                 Name
 *  -----                 ----
 *  3                     Packet Length
 *  1                     Packet Number
 * </p>
 *  <b>Packet Length:</b> The length, in bytes, of the packet
 *                 that follows the Packet Header. There
 *                 may be some special values in the most
 *                 significant byte. Since 2**24 = MB,
 *                 the maximum packet length is 16MB.
 * </P>
 *  <b>Packet Number:</b> A serial number which can be used to
 *                 ensure that all packets are present
 *                 and in order. The first packet of a
 *                 client query will have Packet Number = 0
 *                 Thus, when a new SQL statement starts, 
 *                 the packet number is re-initialised.
 *  
 * </pre>
 * 
 * @see <href a="http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#The_Packet_Header">The_Packet_Header</a>
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * 
 */
public class MysqlFramedInputStream extends PacketInputStream implements MySqlPacketConstant{

	private byte _packetId;
	private boolean readPackedWithHead;
	public byte getPacketId(){
		return _packetId;
	}
	
	public MysqlFramedInputStream(boolean readPackedWithHead){
		this.readPackedWithHead = readPackedWithHead;
	}
	protected int decodeLength() {
		// if we don't have enough bytes to determine our frame size, stop
		// here and let the caller know that we're not ready
		if (_have < getHeaderSize()) {
			return -1;
		}

		// decode the frame length
		_buffer.rewind();
		
		/**
		 * mysql 数据部分＋包头=整个数据包长度
		 */
		int length = (_buffer.get() & 0xff)
					+ ((_buffer.get() & 0xff) << 8)	
					+ ((_buffer.get() & 0xff) << 16)
					+ this.getHeaderSize();
		_packetId = _buffer.get();
		_buffer.position(_have);
		return length;
	}

	public int getHeaderSize() {
		return HEADER_SIZE;
	}
	
	protected boolean checkForCompletePacket ()
    {
        if (_length == -1 || _have < _length) {
            return false;
        }
        //将buffer 包含整个数据包，包括包头内容
        if(readPackedWithHead){
        	_buffer.position(0);
        }else{
        	_buffer.position(this.getHeaderSize());
        }
        _buffer.limit(_length);
        return true;
    }
}