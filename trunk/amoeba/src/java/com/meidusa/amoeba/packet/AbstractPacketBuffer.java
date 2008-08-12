package com.meidusa.amoeba.packet;

import java.nio.ByteBuffer;

/**
 * 
 * @author struct
 *
 */
public class AbstractPacketBuffer implements PacketBuffer {

	protected int length = 0;

	protected int position = 0;

	protected byte[] buffer = null;

	public AbstractPacketBuffer(byte[] buf) {
		buffer = new byte[buf.length + 1];
		System.arraycopy(buf, 0, buffer, 0, buf.length);
		setPacketLength(buf.length);
		position = 0;
	}

	public AbstractPacketBuffer(int size) {
		this.buffer = new byte[size];
		setPacketLength(this.buffer.length);
		this.position = 0;
	}

    /**
     * 将从0当到前位置的所有字节写入到 ByteBuffer中,并且将 ByteBuffer position设置到0
     * 
     * @return
     */
    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(this.getPacketLength());
        buffer.put(this.buffer, 0, this.getPacketLength());
        buffer.rewind();
        return buffer;
    }

    public int getPacketLength() {
        return length;
    }

    public void setPacketLength(int length) {
        this.length = length;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * 增加buffer长度
     */
    protected void ensureCapacity(int len) {
        if ((position + len) > getPacketLength()) {
            if ((position + len) < buffer.length) {
                setPacketLength(buffer.length);
            } else {
                int newLength = (int) (buffer.length * 1.25);

                if (newLength < (buffer.length + len)) {
                    newLength = buffer.length + (int) (len * 1.25);
                }

                byte[] newBytes = new byte[newLength];
                System.arraycopy(buffer, 0, newBytes, 0, buffer.length);
                buffer = newBytes;
                setPacketLength(buffer.length);
            }
        }
    }

    public void writeByte(byte b) {
    	ensureCapacity(1);
        buffer[position++] = b;
    }
	

}
