package com.meidusa.amoeba.gateway.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.meidusa.amoeba.util.StringUtil;


/**
 * 该类负责 发送、接收socket输入流，并且可以根据包头信息，构造出ByteBuffer 该类采用网络字节序进行通讯
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public abstract class GenericIOPacketBuffer extends AbstractGatewayPacketBuffer {
	public abstract int getHeadSize();
	public GenericIOPacketBuffer(byte[] buf) {
		super(buf);
	}

	public GenericIOPacketBuffer(int size) {
		super(size);
	}

	final void dumpHeader() {
		for (int i = 0; i < getHeadSize(); i++) {
			String hexVal = Integer.toHexString(readByte(i) & 0xff);

			if (hexVal.length() == 1) {
				hexVal = "0" + hexVal; //$NON-NLS-1$
			}
			System.out.print(hexVal + " "); //$NON-NLS-1$
		}
	}



	public final byte[] getBytes(int len) {
		byte[] b = new byte[len];
		this.buffer.get(b);
		return b;
	}

	public byte[] getBytes(int offset, int len) {
		byte[] dest = new byte[len];
		System.arraycopy(this.buffer, offset, dest, 0, len);
		return dest;
	}

	/**
	 * 表示后面所要读取内容的长度
	 * 
	 * @return
	 */
	public abstract long readFieldLength();

	public final int readInt() {
		return this.buffer.getInt();
	}

	public final long readLong() {
		return this.buffer.getLong();
	}

	public int writeLengthCodedBytes(byte[] ab) {
    	ensureCapacity(ab.length+4);
    	this.writeInt(ab==null?0:ab.length);
    	if(ab != null && ab.length > 0){
    		return writeBytes(ab, 0, ab.length);
    	}else{
    		return 0;
    	}
    }
	
	public byte[] readLengthCodedBytes() {
		int count = readInt();
		if(count ==0){
			return new byte[0];
		}else{
			byte[] bts = new byte[count];
			this.readBytes(bts, 0, count);
			return bts;
		}
    }
	
	public final String readLengthCodedString(String encoding) {
		int fieldLength = (int) readFieldLength();
		
		if (fieldLength == 0) {
			return null;
		}
		
		if(fieldLength> this.buffer.remaining()){
			throw new RuntimeException("fieldLength error Buffer.Remaining="+buffer.remaining()+" ,but need size="+ fieldLength);
		}
		byte[] bytes = getBytes(fieldLength);
		try {
			if (encoding != null) {
				return new String(bytes,encoding);
			} else {
				return new String(bytes);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO log exception
			return new String(bytes);
		} 
	}

	public String toString() {
		return StringUtil.dumpAsHex(this.buffer.array(),getPosition());
	}

	public String toSuperString() {
		return super.toString();
	}

	public double readDouble() {
		return this.buffer.getDouble();
	}

	public abstract void writeFieldLength(int length);

	public final void writeFloat(float f) {
		ensureCapacity(8);
		this.buffer.putFloat(f);
	}

	public final float readFloat() {
		return this.buffer.getFloat();
	}

	public final void writeInt(int i) {
		ensureCapacity(4);
		this.buffer.putInt(i);
	}

	public final void writeLengthCodedString(String s, String encoding) {
		if (s != null) {
			byte[] b;
			try {
				b = s.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				// TODO
				e.printStackTrace();
				b = s.getBytes();
			}
			ensureCapacity(b.length + 9);
			this.writeFieldLength(b.length);
			this.writeBytes(b);
		} else {
			this.writeFieldLength(0);
		}
	}

	public final void writeLong(long i) {
		ensureCapacity(8);
		this.buffer.putLong(i);
	}

	// Write null-terminated string
	public final void writeString(String s) {
		try {
			writeString(s, null);
		} catch (UnsupportedEncodingException e) {
		}
	}

	public final void writeString(String s, String encoding)
			throws UnsupportedEncodingException {
		byte[] bytes = null;
		if (encoding == null) {
			bytes = s.getBytes();
		} else {
			bytes = s.getBytes(encoding);
		}
		ensureCapacity(bytes.length + 1 + 8);
		this.writeFieldLength(bytes.length);
		this.writeBytes(bytes);
	}

	public static void main(String[] args) {
		GenericIOPacketBuffer buffer = new GenericIOPacketBuffer(34) {
			@Override
			public int getHeadSize() {
				return 0;
			}
			@Override
			public long readFieldLength() {
				return 0;
			}
			@Override
			public void writeFieldLength(int length) {
			}
		};

		buffer.writeLong(44);
		// System.out.println(buffer.dump(34));
		ByteBuffer bytbuffer = ByteBuffer.allocate(32);
		bytbuffer.putLong(44);
		System.out.println(StringUtil.dumpAsHex(bytbuffer.array(), 32));
	}
}
