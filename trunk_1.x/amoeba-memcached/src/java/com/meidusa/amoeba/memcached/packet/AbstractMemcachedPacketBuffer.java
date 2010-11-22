package com.meidusa.amoeba.memcached.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public class AbstractMemcachedPacketBuffer extends AbstractPacketBuffer {

	public AbstractMemcachedPacketBuffer(byte[] buf) {
		super(buf);
	}

	public AbstractMemcachedPacketBuffer(int size) {
		super(size);
	}

	public void writeShort(short value) {
		ensureCapacity(2);
		buffer[position++] = (byte) ((value >>> 8) & 0xff);
		buffer[position++] = (byte) (value & 0xff);
	}

	public short readShort() {
		byte[] b = this.buffer; // a little bit optimization
		return (short)(((b[this.position++] & 0xff) << 8) | (b[this.position++] & 0xff));
	}
	
	public int readInt() {
		byte[] b = this.buffer; // a little bit optimization
		return ((b[this.position++] & 0xff) << 24) |((b[this.position++] & 0xff) << 16) 
			   |((b[this.position++] & 0xff) << 8) | (b[this.position++] & 0xff);
	}


	public void writeInt(int value) {
		ensureCapacity(4);
		buffer[position++] = (byte) ((value >>> 24) & 0xff);
		buffer[position++] = (byte) ((value >>> 16) & 0xff);
		buffer[position++] = (byte) ((value >>> 8) & 0xff);
		buffer[position++] = (byte) (value & 0xff);
	}

	public static void main(String[] args) {
		System.out.println(Integer.toHexString(2147483647));
	}

}
