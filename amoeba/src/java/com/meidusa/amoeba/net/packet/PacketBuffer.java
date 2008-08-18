package com.meidusa.amoeba.net.packet;

import java.nio.ByteBuffer;

/**
 * 
 * @author struct
 *
 */
public interface PacketBuffer {
	
	/**
	 * 
	 * @return
	 */
	public ByteBuffer toByteBuffer();
	
	public byte readByte();

	public byte readByte(int postion);
	/**
	 * 
	 * @param bte
	 */
	public void writeByte(byte bte);
	/**
	 * 
	 * @return
	 */
	public int getPacketLength();
	
	
	/**
	 * 
	 * @return
	 */
	public int getPosition();
	
	/**
	 * Set the current position to write to/ read from
	 * 
	 * @param position
	 *            the position (0-based index)
	 */
	public void setPosition(int positionToSet);
	
}
