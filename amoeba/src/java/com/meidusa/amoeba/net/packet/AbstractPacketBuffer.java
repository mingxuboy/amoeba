package com.meidusa.amoeba.net.packet;

import java.io.InputStream;
import java.io.OutputStream;
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
        buffer = new byte[buf.length];
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
        /*
         * byte[] newbyte = new byte[getPosition()]; System.arraycopy(this.buffer, 0, newbyte, 0, this.getPosition());
         * ByteBuffer buffer = ByteBuffer.wrap(newbyte); buffer.rewind(); return buffer;
         */
        ByteBuffer buffer = ByteBuffer.allocate(getPosition());
        buffer.put(this.buffer, 0, getPosition());
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
        if (this.position < position) {
            int length = this.position - position;
            ensureCapacity(length);
        }
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

    protected void init(Connection conn) {
    }

    public synchronized void reset() {
        this.position = 0;
        this.length = 0;
    }

    public int remaining() {
        return this.length - this.position;
    }

    public boolean hasRemaining() {
        return (this.length - this.position > 0);
    }

    public void skip(int bytes) {
        this.position += bytes;
    }

    public InputStream asInputStream() {
        return new InputStream() {

            @Override
            public int available() {
                return AbstractPacketBuffer.this.remaining();
            }

            @Override
            public int read() {
                if (AbstractPacketBuffer.this.hasRemaining()) {
                    return AbstractPacketBuffer.this.readByte() & 0xff;
                } else {
                    return -1;
                }
            }

            @Override
            public int read(byte[] b, int off, int len) {
                int remaining = AbstractPacketBuffer.this.remaining();
                if (remaining > 0) {
                    int readBytes = Math.min(remaining, len);
                    AbstractPacketBuffer.this.readBytes(b, off, readBytes);
                    return readBytes;
                } else {
                    return -1;
                }
            }

            @Override
            public synchronized void reset() {
                AbstractPacketBuffer.this.reset();
            }

            @Override
            public long skip(long n) {
                int bytes;
                if (n > Integer.MAX_VALUE) {
                    bytes = AbstractPacketBuffer.this.remaining();
                } else {
                    bytes = Math.min(AbstractPacketBuffer.this.remaining(), (int) n);
                }
                AbstractPacketBuffer.this.skip(bytes);
                return bytes;
            }
        };
    }

    public OutputStream asOutputStream() {
        return new OutputStream() {

            @Override
            public void write(byte[] b, int off, int len) {
                AbstractPacketBuffer.this.writeBytes(b, off, len);
            }

            @Override
            public void write(int b) {
                AbstractPacketBuffer.this.writeByte((byte) b);
            }
        };
    }

}
