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
package com.meidusa.amoeba.gateway.io;

import com.meidusa.amoeba.gateway.packet.GatewayPacketConstant;
import com.meidusa.amoeba.net.io.PacketInputStream;

/**
 * <pre>
 * struct MsgHeader {
 *     int32   messageLength; // total message size, including this
 *     int32   requestID;     // identifier for this message
 *     int32   responseTo;    // requestID from the original request
 *                            //   (used in reponses from db)
 *     int32   opCode;        // request type - see table below
 * }
 * </pre>
 * @see <a href="http://www.mongodb.org/display/DOCS/Mongo+Wire+Protocol#MongoWireProtocol-TableOfContents">mongodb wire protocol</a>
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * 
 */
public class GatewayInputStream extends PacketInputStream implements GatewayPacketConstant{

	private boolean readPackedWithHead;
	
	public GatewayInputStream(boolean readPackedWithHead){
		this.readPackedWithHead = readPackedWithHead;
	}
	protected int decodeLength() {
		
		/**
		 * �ж�һ�����ǵ�ǰ�Ѿ���ȡ�����ݰ��������Ƿ�Ȱ�ͷ��,�����:����Լ����������ĳ���,���򷵻�-1
		 */
		if (_have < getHeaderSize()) {
			return -1;
		}

		//_buffer.rewind();
		
		/**
		 * mysql ���ݲ��֣���ͷ=�������ݰ�����
		 */
		int length = ((_buffer.get(0) &0xff) << 24)
					| ((_buffer.get(1) & 0xff) << 16)
					| ((_buffer.get(2) & 0xff) << 8)
					| ((_buffer.get(3) & 0xff) << 0);
		
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