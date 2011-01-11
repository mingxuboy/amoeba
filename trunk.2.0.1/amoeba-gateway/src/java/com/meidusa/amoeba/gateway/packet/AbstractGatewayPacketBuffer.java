package com.meidusa.amoeba.gateway.packet;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author struct
 */
public class AbstractGatewayPacketBuffer extends  com.meidusa.amoeba.net.packet.AbstractPacketBuffer {

    protected ByteBuffer     buffer   = null;

    public AbstractGatewayPacketBuffer(byte[] buf){
    	super(buf);
        buffer = ByteBuffer.wrap(buf);
        buffer.order(getByteOrder());
    }

    public AbstractGatewayPacketBuffer(int size){
    	super(size);
        buffer = ByteBuffer.allocate(size);
        buffer.order(getByteOrder());
    }

    public ByteOrder getByteOrder(){
    	return ByteOrder.BIG_ENDIAN;
    }
    /**
     * 将从0到当前位置的所有字节写入到ByteBuffer中,写完以后将位置设置到以前位置
     */
    public ByteBuffer toByteBuffer() {
    	int position = this.getPosition();
    	byte[] array = new byte[position];
    	this.buffer.position(0);
    	this.buffer.get(array, 0, position);
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.rewind();
        this.buffer.position(position);
        return buffer;
    }

    public int getPacketLength() {
        return buffer.position();
    }
    public int getPosition() {
        return buffer.position();
    }

    public void setPosition(int position) {
        if (buffer.position()<position) {
            ensureCapacity(position - buffer.position());
        }
        buffer.position(position);
    }

    public byte readByte() {
        return buffer.get();
    }

    public byte readByte(int position) {
    	buffer.position(position);
        return buffer.get();
    }

    public void writeByte(byte b) {
        ensureCapacity(1);
        buffer.put(b);
    }
    
    public void writeShort(short b) {
        ensureCapacity(2);
        buffer.putShort(b);
    }
    
    public short readShort(){
    	return buffer.getShort();
    }

    public int writeBytes(byte[] ab) {
    	ensureCapacity(ab.length);
        return writeBytes(ab, 0, ab.length);
    }
    

    public int writeBytes(byte[] ab, int offset, int len) {
        ensureCapacity(len);
        buffer.put(ab, offset, len);
        return len;
    }

    public int readBytes(byte[] ab, int offset, int len) {
    	buffer.get(ab, offset, len);
        return len;
    }

    /**
     * 增加buffer长度
     */
    protected void ensureCapacity(int i) {
        if (buffer.remaining()<i) {
        	int size = buffer.capacity()<<1;
        	while((size - buffer.position())<i){
        		size = size <<1;
        	}
        	
        	ByteBuffer buffer = ByteBuffer.allocate(size);
        	
        	buffer.put((ByteBuffer)this.buffer.flip());
        	this.buffer = buffer;
        }
    }

    protected void init() {
    }

    public synchronized void reset() {
        buffer.rewind();
        buffer.position(0);
    }

    public int remaining() {
        return buffer.remaining();
    }

    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    public void skip(int bytes) {
    	int position = buffer.position();
    	ensureCapacity(bytes);
    	buffer.position(position+bytes);
    }

    public InputStream asInputStream() {
        return new InputStream() {

            @Override
            public int available() {
                return AbstractGatewayPacketBuffer.this.remaining();
            }

            @Override
            public int read() {
                if (AbstractGatewayPacketBuffer.this.hasRemaining()) {
                    return AbstractGatewayPacketBuffer.this.readByte() & 0xff;
                } else {
                    return -1;
                }
            }

            @Override
            public int read(byte[] b, int off, int len) {
                int remaining = AbstractGatewayPacketBuffer.this.remaining();
                if (remaining > 0) {
                    int readBytes = Math.min(remaining, len);
                    AbstractGatewayPacketBuffer.this.readBytes(b, off, readBytes);
                    return readBytes;
                } else {
                    return -1;
                }
            }

            @Override
            public synchronized void reset() {
                AbstractGatewayPacketBuffer.this.reset();
            }

            @Override
            public long skip(long n) {
                int bytes;
                if (n > Integer.MAX_VALUE) {
                    bytes = AbstractGatewayPacketBuffer.this.remaining();
                } else {
                    bytes = Math.min(AbstractGatewayPacketBuffer.this.remaining(), (int) n);
                }
                AbstractGatewayPacketBuffer.this.skip(bytes);
                return bytes;
            }
        };
    }

    public OutputStream asOutputStream() {
        return new OutputStream() {

            @Override
            public void write(byte[] b, int off, int len) {
                AbstractGatewayPacketBuffer.this.writeBytes(b, off, len);
            }

            @Override
            public void write(int b) {
                AbstractGatewayPacketBuffer.this.writeByte((byte) b);
            }
        };
    }
    
    /**
	 * 往buffer中写入固定长度的字节。如果字符串长度不足则补足长度。超过将被截。
	 * @param buffer
	 * @param string 被写入的字符串
	 * @param encoding 采用的编码
	 * @param length 写入固定长度
	 */
	public void writeFixedLengthString(String string,String encoding,int length){
		if(string == null){
			writeBytes(new byte[length]);
		}else{
			byte[] strBytes = null;
			if(encoding != null){
				try {
					strBytes = string.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					strBytes = string.getBytes();
				}
			}else{
				strBytes = string.getBytes();
			}

			if(length<=strBytes.length){
				writeBytes(strBytes,0,length);
			}else{
				writeBytes(strBytes);
				writeBytes(new byte[length-strBytes.length]);
			}
		}
	}
	
	public String readFixedLengthString(String encoding,int length){
		byte[] strBytes = new byte[length];
    	readBytes(strBytes,0,length);
    	int nullIndex = -1;
    	int noNullLength = 0;
    	for(int i=strBytes.length-1;i>=0;i--){
    		if(strBytes[i] != (byte)0){
    			nullIndex = i;
    			break;
    		}
    	}
    	
    	if(nullIndex == 0){
    		return null;
    	}
    	
    	if(nullIndex == -1){
    		noNullLength = length;
    	}else{
    		noNullLength = nullIndex+1;
    	}
    	
    	if(encoding == null){
    		return new String(strBytes,0,noNullLength);
    	}else{
    		try {
				return new String(strBytes,0,noNullLength, encoding);
			} catch (UnsupportedEncodingException e) {
				return new String(strBytes,0,noNullLength);
			}
    	}
	}
	
	public static void main(String[] args){
		byte[] byts = new byte[]{(byte)1,(byte)0,(byte)0,(byte)0,(byte)12,(byte)1,(byte)0,(byte)0};
		AbstractGatewayPacketBuffer buffer = new AbstractGatewayPacketBuffer(byts);
		String ms = buffer.readFixedLengthString(null, 7);
		System.out.println(ms);
	}
}
