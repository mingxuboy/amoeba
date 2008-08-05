package com.meidusa.amoeba.oracle.io;

import com.meidusa.amoeba.net.io.PacketInputStream;

/**
 * 
 * @author struct
 *
 */
public class OraclePacketInputStream extends PacketInputStream implements OraclePacketConstant {
	
	
	@Override
	protected int decodeLength() {
		// if we don't have enough bytes to determine our frame size, stop
		// here and let the caller know that we're not ready
		if (_have < getHeaderSize()) {
			return -1;
		}

		// decode the frame length
		_buffer.rewind();
		
		/**
		 * length = 数据部分＋包头=整个数据包长度
		 */
		int length = (_buffer.get() & 0xff)
					+ ((_buffer.get() & 0xff) << 8);	
		_buffer.position(_have);
		return length;
	}

	@Override
	public int getHeaderSize() {
		return HEADER_SIZE;
	}

}
