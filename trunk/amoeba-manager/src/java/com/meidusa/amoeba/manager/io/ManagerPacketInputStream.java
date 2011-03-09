/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.manager.io;

import com.meidusa.amoeba.manager.ManagerConstant;
import com.meidusa.amoeba.net.io.PacketInputStream;

/**
 * 
 * <b> The Packet Header </b>
 * 
 * <pre>
 * Bytes                 Name
 *  -----                 ----
 *  3                     Packet Length
 *  1                     Packet TYPE
 * </p>
 * 
 * 数据包Packet Length 为 整个数据包长度(包括数据＋header)
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * 
 */
public class ManagerPacketInputStream extends PacketInputStream implements ManagerConstant{

	private boolean readPackedWithHead;
	
	public ManagerPacketInputStream(boolean readPackedWithHead){
		this.readPackedWithHead = readPackedWithHead;
	}
	
	protected int decodeLength() {
		
		/**
		 * 判断一下我们当前已经读取的数据包的数据是否比包头长,如果是:则可以计算整个包的长度,否则返回-1
		 */
		if (_have < getHeaderSize()) {
			return -1;
		}

		//_buffer.rewind();
		
		/**
		 * mysql 数据部分＋包头=整个数据包长度
		 */
		int length = (_buffer.get(0) & 0xff)
					| ((_buffer.get(1) & 0xff) << 8)
					| ((_buffer.get(2) & 0xff) << 16)
					| ((_buffer.get(3) & 0xff) << 24);
		
		return length;
	}


	public int getHeaderSize() {
		return HEADER_SIZE;
	}
	
	protected byte[] readPacket(){
        byte[] msg = new byte[_length];
        int position = _buffer.position();
        if(readPackedWithHead){
        	_buffer.position(0);
        }else{
        	_buffer.position(this.getHeaderSize());
        }
        _buffer.get(msg, 0, _length);
    	try{
    		_buffer.limit(_have);
    		
    		_buffer.compact();
    		_buffer.position(position - _length);
            _have -= _length;
            _length = this.decodeLength();
    	}catch(IllegalArgumentException e){
    		throw new IllegalArgumentException("old position="+_buffer.position()+", new position="+_length+",old limit="+_buffer.limit() +", have(new limit)="+_have,e);
    	}
        return msg;
    }
}