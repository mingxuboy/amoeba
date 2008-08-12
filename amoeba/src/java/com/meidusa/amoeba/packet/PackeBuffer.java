package com.meidusa.amoeba.packet;

import java.nio.ByteBuffer;

/**
 * 
 * @author struct
 *
 */
public interface PackeBuffer {
	
	/**
	 * 
	 * @return
	 */
	public ByteBuffer toByteBuffer();
	
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
