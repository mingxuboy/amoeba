package com.meidusa.amoeba.memcached.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.memcached.MemcachedConstant;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public class AbstractMemcachedPacket extends com.meidusa.amoeba.net.packet.AbstractPacket implements MemcachedConstant {
	
	/**
	 * Magic number
	 */
	public byte magic;
	
	/**
	 * Command code
	 */
	public byte opCode;
	
	/**
	 * Length in bytes of the text key that follows the command extras. 
	 */
	public short keyLength;
	
	/**
	 * Length in bytes of the command extras.
	 */
	public byte extrasLength;
	
	/**
	 * Reserved for future use (Sean is using this soon). 
	 */
	public byte dateType;

	/**
	 * Really reserved for future use (up for grabs). 
	 */
	public short  status; //Request header was Reserved 
	
	/**
	 * Length in bytes of extra + key + value.
	 */
	public int totalBodyLength;
	
	/**
	 * Will be copied back to you in the response
	 */
	public int opaque;
	
	/**
	 * Data version check. 
	 */
	public int cas;
	
	@Override
	protected void afterPacketWritten(AbstractPacketBuffer buffer) {
		/*int position = buffer.getPosition();
		buffer.setPosition(position)
		
		buffer.setPosition(position);*/
	}
	@Override
	protected int calculatePacketSize() {
		return 24;
	}
	@Override
	protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
		return AbstractMemcachedPacketBuffer.class;
	}
	
	@Override
	protected void init(AbstractPacketBuffer buffer) {
		AbstractMemcachedPacketBuffer bufferTemp = (AbstractMemcachedPacketBuffer)buffer;
		magic = bufferTemp.readByte();
		opCode = bufferTemp.readByte();
		keyLength = bufferTemp.readShort();
		extrasLength = bufferTemp.readByte();
		dateType = bufferTemp.readByte();
		status = bufferTemp.readShort();
		totalBodyLength = bufferTemp.readInt();
		opaque = bufferTemp.readInt();
		cas = bufferTemp.readInt();
	}
	
	
	@Override
	protected void write2Buffer(AbstractPacketBuffer buffer)
			throws UnsupportedEncodingException {
		AbstractMemcachedPacketBuffer bufferTemp = (AbstractMemcachedPacketBuffer)buffer;
		bufferTemp.writeByte(magic);
		bufferTemp.writeByte(opCode);
		bufferTemp.writeShort(keyLength);
		bufferTemp.writeByte(extrasLength);
		bufferTemp.writeByte(dateType);
		bufferTemp.writeShort(status);
		bufferTemp.writeInt(totalBodyLength);
		bufferTemp.writeInt(opaque);
		bufferTemp.writeInt(cas);
	}
}
