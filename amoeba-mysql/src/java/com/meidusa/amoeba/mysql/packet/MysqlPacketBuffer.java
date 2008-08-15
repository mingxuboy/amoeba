/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mysql.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import java.sql.SQLException;

import com.meidusa.amoeba.mysql.io.Constants;
import com.meidusa.amoeba.mysql.io.MySqlPacketConstant;
import com.meidusa.amoeba.mysql.util.MysqlStringUtil;
import com.meidusa.amoeba.mysql.util.SingleByteCharsetConverter;
import com.meidusa.amoeba.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * 该类负责 发送、接收 socket 输入流，并且可以根据包头信息，构造出ByteBuffer
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class MysqlPacketBuffer extends AbstractPacketBuffer{
	static final int MAX_BYTES_TO_DUMP = 512;

	static final int NO_LENGTH_LIMIT = -1;

	static final long NULL_LENGTH = -1;

	protected boolean wasMultiPacket = false;
	
	/**
	 * buf 中含包头信息
	 * @param buf
	 */
	public MysqlPacketBuffer(byte[] buf) {
		
		super(buf);
		this.position = MySqlPacketConstant.HEADER_SIZE;
	}
	
	public int getPacketLength(){
		if(buffer == null || buffer.length <4){
			return 0;
		}else{
			int packetLength = (buffer[0] & 0xff) +
            ((buffer[1] & 0xff) << 8) +
            ((buffer[2] & 0xff) << 16);
			return packetLength;
		}
	}
	
	public static int getPacketLength(byte[] byteBuffer){
		if(byteBuffer == null || byteBuffer.length <4){
			return 0;
		}else{
			int packetLength = (byteBuffer[0] & 0xff) +
            ((byteBuffer[1] & 0xff) << 8) +
            ((byteBuffer[2] & 0xff) << 16);
			return packetLength;
		}
	}
	
	public MysqlPacketBuffer(int size) {
		super(size);
		this.position = MySqlPacketConstant.HEADER_SIZE;
	}

	final void clear() {
		this.position = MySqlPacketConstant.HEADER_SIZE;
	}

	final void dump() {
		dump(getBufLength());
	}

	final String dump(int numBytes) {
		return StringUtil.dumpAsHex(getBytes(0,
				numBytes > getBufLength() ? getBufLength() : numBytes),
				numBytes > getBufLength() ? getBufLength() : numBytes);
	}

	final String dumpClampedBytes(int numBytes) {
		int numBytesToDump = numBytes < MAX_BYTES_TO_DUMP ? numBytes
				: MAX_BYTES_TO_DUMP;

		String dumped = StringUtil.dumpAsHex(getBytes(0,
				numBytesToDump > getBufLength() ? getBufLength()
						: numBytesToDump),
				numBytesToDump > getBufLength() ? getBufLength()
						: numBytesToDump);

		if (numBytesToDump < numBytes) {
			return dumped + " ....(packet exceeds max. dump length)";
		}

		return dumped;
	}

	final void dumpHeader() {
		for (int i = 0; i < MySqlPacketConstant.HEADER_SIZE; i++) {
			String hexVal = Integer.toHexString(readByte(i) & 0xff);

			if (hexVal.length() == 1) {
				hexVal = "0" + hexVal; //$NON-NLS-1$
			}

			System.out.print(hexVal + " "); //$NON-NLS-1$
		}
	}

	final void dumpNBytes(int start, int nBytes) {
		StringBuffer asciiBuf = new StringBuffer();

		for (int i = start; (i < (start + nBytes)) && (i < getBufLength()); i++) {
			String hexVal = Integer.toHexString(readByte(i) & 0xff);

			if (hexVal.length() == 1) {
				hexVal = "0" + hexVal; //$NON-NLS-1$
			}

			System.out.print(hexVal + " "); //$NON-NLS-1$

			if ((readByte(i) > 32) && (readByte(i) < 127)) {
				asciiBuf.append((char) readByte(i));
			} else {
				asciiBuf.append("."); //$NON-NLS-1$
			}

			asciiBuf.append(" "); //$NON-NLS-1$
		}

		System.out.println("    " + asciiBuf.toString()); //$NON-NLS-1$
	}

	protected void ensureCapacity(int additionalData){
		if ((this.position + additionalData) > getBufLength()) {
			if ((this.position + additionalData) < this.buffer.length) {
				// byteBuffer.length is != getBufLength() all of the time
				// due to re-using of packets (we don't shrink them)
				//
				// If we can, don't re-alloc, just set buffer length
				// to size of current buffer
				setBufLength(this.buffer.length);
			} else {
				//
				// Otherwise, re-size, and pad so we can avoid
				// allocing again in the near future
				//
				int newLength = (int) (this.buffer.length * 1.25);

				if (newLength < (this.buffer.length + additionalData)) {
					newLength = this.buffer.length
							+ (int) (additionalData * 1.25);
				}

				if (newLength < this.buffer.length) {
					newLength = this.buffer.length + additionalData;
				}

				byte[] newBytes = new byte[newLength];

				System.arraycopy(this.buffer, 0, newBytes, 0,
						this.buffer.length);
				this.buffer = newBytes;
				setBufLength(this.buffer.length);
			}
		}
	}

	/**
	 * Skip over a length-encoded string
	 * 
	 * @return The position past the end of the string
	 */
	public int fastSkipLenString() {
		long len = this.readFieldLength();

		this.position += len;

		return (int) len;
	}

	public void fastSkipLenByteArray() {
		long len = this.readFieldLength();

		if (len == NULL_LENGTH || len == 0) {
			return;
		}
		
		this.position += len;
	}
	
	protected final byte[] getBufferSource() {
		return this.buffer;
	}

	int getBufLength() {
		return this.length;
	}

	/**
	 * Returns the array of bytes this Buffer is using to read from.
	 * 
	 * @return byte array being read from
	 */
	public byte[] getByteBuffer() {
		return this.buffer;
	}

	public final byte[] getBytes(int len) {
		byte[] b = new byte[len];
		System.arraycopy(this.buffer, this.position, b, 0, len);
		this.position += len; // update cursor

		return b;
	}

	public byte[] getBytes(int offset, int len) {
		byte[] dest = new byte[len];
		System.arraycopy(this.buffer, offset, dest, 0, len);

		return dest;
	}

	int getCapacity() {
		return this.buffer.length;
	}


	final boolean isLastDataPacket() {
		return ((getBufLength() < 9) && ((this.buffer[4] & 0xff) == 254));
	}

	final long newReadLength() {
		int sw = this.buffer[this.position++] & 0xff;

		switch (sw) {
		case 251:
			return 0;

		case 252:
			return readInt();

		case 253:
			return readLongInt();

		case 254: // changed for 64 bit lengths
			return readLongLong();

		default:
			return sw;
		}
	}

	final long readFieldLength() {
		int sw = this.buffer[this.position++] & 0xff;

		switch (sw) {
		case 251:
			return NULL_LENGTH;

		case 252:
			return readInt();

		case 253:
			return readLongInt();

		case 254:
			return readLongLong();

		default:
			return sw;
		}
	}

	final int readInt() {
		byte[] b = this.buffer; // a little bit optimization

		return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8);
	}

	final int readIntAsLong() {
		byte[] b = this.buffer;

		return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8)
				| ((b[this.position++] & 0xff) << 16)
				| ((b[this.position++] & 0xff) << 24);
	}

	final byte[] readLenByteArray(int offset) {
		long len = this.readFieldLength();

		if (len == NULL_LENGTH) {
			return null;
		}

		if (len == 0) {
			return Constants.EMPTY_BYTE_ARRAY;
		}

		this.position += offset;

		return getBytes((int) len);
	}

	final long readLength() {
		int sw = this.buffer[this.position++] & 0xff;

		switch (sw) {
		case 251:
			return 0;

		case 252:
			return readInt();

		case 253:
			return readLongInt();

		case 254:
			return readLong();

		default:
			return sw;
		}
	}

	final long readLong() {
		byte[] b = this.buffer;

		return ((long) b[this.position++] & 0xff)
				| (((long) b[this.position++] & 0xff) << 8)
				| ((long) (b[this.position++] & 0xff) << 16)
				| ((long) (b[this.position++] & 0xff) << 24);
	}

	final int readLongInt() {
		byte[] b = this.buffer;

		return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8)
				| ((b[this.position++] & 0xff) << 16);
	}

	final long readLongLong() {
		byte[] b = this.buffer;

		return (b[this.position++] & 0xff)
				| ((long) (b[this.position++] & 0xff) << 8)
				| ((long) (b[this.position++] & 0xff) << 16)
				| ((long) (b[this.position++] & 0xff) << 24)
				| ((long) (b[this.position++] & 0xff) << 32)
				| ((long) (b[this.position++] & 0xff) << 40)
				| ((long) (b[this.position++] & 0xff) << 48)
				| ((long) (b[this.position++] & 0xff) << 56);
	}

	final int readnBytes() {
		int sw = this.buffer[this.position++] & 0xff;

		switch (sw) {
		case 1:
			return this.buffer[this.position++] & 0xff;

		case 2:
			return this.readInt();

		case 3:
			return this.readLongInt();

		case 4:
			return (int) this.readLong();

		default:
			return 255;
		}
	}

	//
	// Read a null-terminated string
	//
	// To avoid alloc'ing a new byte array, we
	// do this by hand, rather than calling getNullTerminatedBytes()
	//
	final String readString() {
		int i = this.position;
		int len = 0;
		int maxLen = getBufLength();

		while ((i < maxLen) && (this.buffer[i] != 0)) {
			len++;
			i++;
		}

		String s = new String(this.buffer, this.position, len);
		this.position += (len + 1); // update cursor

		return s;
	}
	
	final String readLengthCodedString(String encoding){
		int length = (int)this.readFieldLength();
		if(length ==0) return null;
		try {
			if(encoding != null){
				return new String(this.buffer, this.position, (int)length, encoding);
			}else{
				return new String(this.buffer, this.position, length);
			}
		}catch(UnsupportedEncodingException e){
			//TODO logger exception
			return new String(this.buffer, this.position, length);
		} finally {
			this.position += length; // update cursor
		}
	}
	
	public static boolean isErrorPacket(byte[] bty){
		return isPacketType(bty, (byte)0xff);
	}
	
	public static boolean isEofPacket(byte[] bty){
		return isPacketType(bty,(byte)0xfe);
	}
	
	public static int increasePacketId(int packetId){
		if(packetId >= 255){
			return 0;
		}else{
			return packetId++;
		}
	}
	public static boolean isPacketType(byte[] bytes,byte type){
		if(bytes.length>=5){
			return bytes[4] == type;
		}
		return false;
	}
	
	public static boolean isOkPacket(byte[] bty){
		return isPacketType(bty, (byte)0x00);
	}
	
	final String readString(String encoding){
		int i = this.position;
		int len = 0;
		int maxLen = getBufLength();

		while ((i < maxLen) && (this.buffer[i] != 0)) {
			len++;
			i++;
		}

		try {
			return new String(this.buffer, this.position, len, encoding);
		}catch(UnsupportedEncodingException e){
			//TODO logger exception
			return new String(this.buffer, this.position, len);
		} finally {
			this.position += (len + 1); // update cursor
		}
	}

	void setBufLength(int bufLengthToSet) {
		this.length = bufLengthToSet;
	}

	/**
	 * Sets the array of bytes to use as a buffer to read from.
	 * 
	 * @param buffer
	 *            the array of bytes to use as a buffer
	 */
	public void setByteBuffer(byte[] byteBufferToSet) {
		this.buffer = byteBufferToSet;
	}

	/**
	 * Sets whether this packet was part of a multipacket
	 * 
	 * @param flag
	 *            was this packet part of a multipacket?
	 */
	public void setWasMultiPacket(boolean flag) {
		this.wasMultiPacket = flag;
	}

	public String toString() {
		return dumpClampedBytes(getPosition());
	}

	public String toSuperString() {
		return super.toString();
	}

	/**
	 * Was this packet part of a multipacket?
	 * 
	 * @return was this packet part of a multipacket?
	 */
	public boolean wasMultiPacket() {
		return this.wasMultiPacket;
	}

	// Write a byte array
	final void writeBytesNoNull(byte[] bytes){
		int len = bytes.length;
		ensureCapacity(len);
		System.arraycopy(bytes, 0, this.buffer, this.position, len);
		this.position += len;
	}

	// Write a byte array with the given offset and length
	final void writeBytesNoNull(byte[] bytes, int offset, int length)
			throws SQLException {
		ensureCapacity(length);
		System.arraycopy(bytes, offset, this.buffer, this.position, length);
		this.position += length;
	}

	public final void writeDouble(double d){
		long l = Double.doubleToLongBits(d);
		writeLongLong(l);
	}
	
	public double readDouble(){
		long result = readLongLong();
		return Double.longBitsToDouble(result);
	}

	public final void writeFieldLength(long length){
		if (length < 251) {
			writeByte((byte) length);
		} else if (length < 65536L) {
			ensureCapacity(3);
			writeByte((byte) 252);
			writeInt((int) length);
		} else if (length < 16777216L) {
			ensureCapacity(4);
			writeByte((byte) 253);
			writeLongInt((int) length);
		} else {
			ensureCapacity(9);
			writeByte((byte) 254);
			writeLongLong(length);
		}
	}

	public final void writeFloat(float f){
		ensureCapacity(4);

		int i = Float.floatToIntBits(f);
		byte[] b = this.buffer;
		b[this.position++] = (byte) (i & 0xff);
		b[this.position++] = (byte) (i >>> 8);
		b[this.position++] = (byte) (i >>> 16);
		b[this.position++] = (byte) (i >>> 24);
	}
	
	final float readFloat(){
		byte[] b = this.buffer;

		int result =((int) b[this.position++] & 0xff)
				| (((int) b[this.position++] & 0xff) << 8)
				| ((int) (b[this.position++] & 0xff) << 16)
				| ((int) (b[this.position++] & 0xff) << 24);
		return Float.intBitsToFloat(result);
	}
	
	// 2000-06-05 Changed
	final void writeInt(int i){
		ensureCapacity(2);

		byte[] b = this.buffer;
		b[this.position++] = (byte) (i & 0xff);
		b[this.position++] = (byte) (i >>> 8);
	}

	// Write a String using the specified character
	// encoding
	final void writeLenBytes(byte[] b){
		int len = b.length;
		ensureCapacity(len + 9);
		writeFieldLength(len);
		System.arraycopy(b, 0, this.buffer, this.position, len);
		this.position += len;
	}

	final void writeLengthCodedString(String s,String encoding) throws UnsupportedEncodingException{
		if(s != null){
			byte[] b = s.getBytes(encoding);
			ensureCapacity(b.length + 9);
			this.writeFieldLength(b.length);
			this.writeBytesNoNull(b);
		}else{
			this.writeByte((byte)0);
		}
	}
	// Write a String using the specified character
	// encoding
	final void writeLenString(String s, String encoding, String serverEncoding,
			SingleByteCharsetConverter converter, boolean parserKnowsUnicode)
			throws UnsupportedEncodingException{
		byte[] b = null;

		if (converter != null) {
			b = converter.toBytes(s);
		} else {
			b = MysqlStringUtil.getBytes(s,converter, encoding, serverEncoding,
					parserKnowsUnicode);
		}

		int len = b.length;
		ensureCapacity(len + 9);
		writeFieldLength(len);
		System.arraycopy(b, 0, this.buffer, this.position, len);
		this.position += len;
	}

	final void writeLong(long i){
		ensureCapacity(4);

		byte[] b = this.buffer;
		b[this.position++] = (byte) (i & 0xff);
		b[this.position++] = (byte) (i >>> 8);
		b[this.position++] = (byte) (i >>> 16);
		b[this.position++] = (byte) (i >>> 24);
	}

	final void writeLongInt(int i){
		ensureCapacity(3);
		byte[] b = this.buffer;
		b[this.position++] = (byte) (i & 0xff);
		b[this.position++] = (byte) (i >>> 8);
		b[this.position++] = (byte) (i >>> 16);
	}

	final void writeLongLong(long i){
		ensureCapacity(8);
		byte[] b = this.buffer;
		b[this.position++] = (byte) (i & 0xff);
		b[this.position++] = (byte) (i >>> 8);
		b[this.position++] = (byte) (i >>> 16);
		b[this.position++] = (byte) (i >>> 24);
		b[this.position++] = (byte) (i >>> 32);
		b[this.position++] = (byte) (i >>> 40);
		b[this.position++] = (byte) (i >>> 48);
		b[this.position++] = (byte) (i >>> 56);
	}

	// Write null-terminated string
	final void writeString(String s){
		ensureCapacity((s.length() * 2) + 1);
		writeStringNoNull(s);
		this.buffer[this.position++] = 0;
	}
	
	//	 Write null-terminated string in the given encoding
	final void writeString(String s, String encoding) throws UnsupportedEncodingException{
		ensureCapacity((s.length() * 2) + 1);
		writeStringNoNull(s, encoding, encoding, false);
		this.buffer[this.position++] = 0;
	}

	// Write string, with no termination
	final void writeStringNoNull(String s){
		int len = s.length();
		ensureCapacity(len * 2);
		System.arraycopy(s.getBytes(), 0, this.buffer, this.position, len);
		this.position += len;
	}

	// Write a String using the specified character
	// encoding
	final void writeStringNoNull(String s, String encoding,
			String serverEncoding, boolean parserKnowsUnicode) throws UnsupportedEncodingException{
		byte[] b = null;
		SingleByteCharsetConverter converter = SingleByteCharsetConverter.getInstance(encoding);
		b = MysqlStringUtil.getBytes(s, converter,encoding, serverEncoding,
				parserKnowsUnicode);
		
		int len = b.length;
		ensureCapacity(len);
		System.arraycopy(b, 0, this.buffer, this.position, len);
		this.position += len;
	}
	
	/**
	 * 将从0当到前位置的所有字节写入到 ByteBuffer中,并且将 ByteBuffer position设置到0
	 * @return
	 */
	public ByteBuffer toByteBuffer(){
		ByteBuffer buffer = ByteBuffer.allocate(this.getPacketLength()+4);
		buffer.put(this.buffer,0,this.getPacketLength()+4);
		buffer.rewind();
		return buffer;
	}
}
