package com.meidusa.amoeba.oracle.packet;

import java.nio.ByteBuffer;

import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.packet.PacketBuffer;

public class T4CPacketBuffer implements PacketBuffer, OraclePacketConstant {

    private int    length   = 0;

    private int    position = 0;

    private byte[] buffer   = null;

    public T4CPacketBuffer(byte[] buf){
        buffer = new byte[buf.length + 1];
        System.arraycopy(buf, 0, buffer, 0, buf.length);
        setPacketLength(buffer.length);
        position = 0;
    }

    public T4CPacketBuffer(int size){
        buffer = new byte[size];
        setPacketLength(buffer.length);
        position = 0;
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

    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(this.getPacketLength());
        buffer.put(this.buffer, 0, this.getPacketLength());
        buffer.rewind();
        return buffer;
    }

    public void writeByte(byte bte) {
        // TODO Auto-generated method stub

    }

}
