package com.meidusa.amoeba.net.packet;

import java.nio.ByteBuffer;

import com.meidusa.amoeba.net.Connection;

/**
 * @author struct
 */
public class AbstractPacketBuffer implements PacketBuffer {
	
    protected int    length   = 0;

    protected int    position = 0;
    
    protected byte[] buffer   = null;

    public AbstractPacketBuffer(byte[] buf){
        buffer = new byte[buf.length + 1];
        System.arraycopy(buf, 0, buffer, 0, buf.length);
        setPacketLength(buffer.length);
        position = 0;
    }

    public AbstractPacketBuffer(int size){
        buffer = new byte[size];
        setPacketLength(buffer.length);
        position = 0;
    }

    /**
     * 将从0到当前位置的所有字节写入到ByteBuffer中,并且将ByteBuffer.position设置到0.
     */
    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(getPacketLength());
        buffer.put(this.buffer, 0, getPacketLength());
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
        int length = this.position - position;
        ensureCapacity(length);
        this.position = position;
    }

    public byte readByte() {
        return buffer[position++];
    }
    
    public byte readByte(int position) {
    	this.position = position;
        return buffer[this.position++];
    }
    
    public void writeByte(byte b) {
        ensureCapacity(1);
        buffer[position++] = b;
    }

    public int writeBytes(byte[] ab) {
        return writeBytes(ab, 0, ab.length);
    }
    
    public int writeBytesWithNull(byte[] ab) {
        int count = writeBytes(ab, 0, ab.length);
        writeByte((byte)0);
        return count;
    }

    public int writeBytes(byte[] ab, int offset, int len) {
        ensureCapacity(len);
        System.arraycopy(ab, offset, buffer, position, len);
        position += len;
        return len;
    }

    public int readBytes(byte[] ab, int offset, int len) {
        System.arraycopy(buffer, position, ab, offset, len);
        position += len;
        return len;
    }

    /**
     * 增加buffer长度
     */
    protected void ensureCapacity(int i) {
        if ((position + i) > getPacketLength()) {
            if ((position + i) < buffer.length) {
                setPacketLength(buffer.length);
            } else {
                int newLength = (int) (buffer.length * 1.25);

                if (newLength < (buffer.length + i)) {
                    newLength = buffer.length + (int) (i * 1.25);
                }

                byte[] newBytes = new byte[newLength];
                System.arraycopy(buffer, 0, newBytes, 0, buffer.length);
                buffer = newBytes;
                setPacketLength(buffer.length);
            }
        }
    }

    
    public void init(Connection conn){
    }
}
