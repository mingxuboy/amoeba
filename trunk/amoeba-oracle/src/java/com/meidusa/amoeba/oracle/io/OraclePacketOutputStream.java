package com.meidusa.amoeba.oracle.io;

import java.nio.ByteBuffer;

import com.meidusa.amoeba.net.io.PacketOutputStream;

public class OraclePacketOutputStream extends PacketOutputStream implements OraclePacketConstant{

	/**
	 * 
	 * @param packetwrittenWithHead 写数据的时候是否写入包头信息，true--表示写入数据的时候已经包括了包头信息，
	 * 							   否则则需要在调用{@link #returnPacketBuffer()} 的时候需要实时生成包头信息
	 */
	public OraclePacketOutputStream(boolean packetwrittenWithHead){
		this.packetwrittenWithHead = packetwrittenWithHead;
		resetPacket();
	}
	
	protected boolean packetwrittenWithHead;
	

	public ByteBuffer returnPacketBuffer ()
    {
        // flip the buffer which will limit it to it's current position
        _buffer.flip();
        _buffer.rewind();
        return _buffer;
    }
	/**
	 * 
	 */
	protected void initHeader(){
		if(!packetwrittenWithHead){
			_buffer.put(HEADER_PAD);
		}
    }

}
