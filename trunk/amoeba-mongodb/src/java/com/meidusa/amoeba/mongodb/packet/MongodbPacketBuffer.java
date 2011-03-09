/*
 * Copyright amoeba.meidusa.com
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mongodb.packet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.bson.BSONDecoder;
import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.io.BasicOutputBuffer;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * 
 * @author Struct
 *
 */
public class MongodbPacketBuffer extends AbstractPacketBuffer {
	static ThreadLocal<BSONDecoder> DECODER = new ThreadLocal<BSONDecoder>(){
		public BSONDecoder initialValue(){
			return new BSONDecoder();
		}
	}; 
	
	static ThreadLocal<BSONEncoder> ENCODER = new ThreadLocal<BSONEncoder>(){
		public BSONEncoder initialValue(){
			return new BSONEncoder();
		}
	}; 
	
	public MongodbPacketBuffer(byte[] buf) {
		super(buf);
	}

	public MongodbPacketBuffer(int size) {
		super(size);
	}

	public int readInt() {
		byte[] b = this.buffer; // a little bit optimization
		return 	   (b[this.position++] & 0xff) 
				| ((b[this.position++] & 0xff) << 8)
				| ((b[this.position++] & 0xff) << 16)
				| ((b[this.position++] & 0xff) << 24);
	}

	public void writeInt(int x) {
		ensureCapacity(4);
		writeByte((byte) (0xFF & x ));
		writeByte((byte) (0xFF & (x >> 8)));
		writeByte((byte) (0xFF & (x >> 16)));
		writeByte((byte) (0xFF & (x >> 24)));
	}

	public void writeInt(int pos, int x) {
		final int save = getPosition();
		setPosition(pos);
		writeInt(x);
		setPosition(save);
	}

	public long readLong() {
		byte[] b = this.buffer; // a little bit optimization
		return (b[this.position++] & 0xffL)
				| ((b[this.position++] & 0xffL) << 8)
				| ((b[this.position++] & 0xffL) << 16)
				| ((b[this.position++] & 0xffL) << 24)
				| ((b[this.position++] & 0xffL) << 32)
				| ((b[this.position++] & 0xffL) << 40)
				| ((b[this.position++] & 0xffL) << 48)
				| ((b[this.position++] & 0xffL) << 56);
	}

	public void writeLong(long x) {
		ensureCapacity(8);
		writeByte((byte) (0xFFL & (x >> 0)));
		writeByte((byte) (0xFFL & (x >> 8)));
		writeByte((byte) (0xFFL & (x >> 16)));
		writeByte((byte) (0xFFL & (x >> 24)));
		writeByte((byte) (0xFFL & (x >> 32)));
		writeByte((byte) (0xFFL & (x >> 40)));
		writeByte((byte) (0xFFL & (x >> 48)));
		writeByte((byte) (0xFFL & (x >> 56)));
	}

	public void writeCString(String content) throws UnsupportedEncodingException{
		byte[] ab = content.getBytes("utf-8");
		ensureCapacity(ab.length+1);
		writeBytes(ab, 0, ab.length);
		writeByte((byte)0);
	}
	
	public String readCString(){
		byte[] b = this.buffer; // a little bit optimization
		int save = this.position;
		while(this.position<b.length){
			if(b[position++] == (byte)0){
				try {
					return new String(b,save,this.position-save-1,"UTF-8");
				} catch (UnsupportedEncodingException e) {
					return new String(b,save,this.position-save-1);
				}
			}else{
				continue;
			}
		}
		return null;
		
	}
	
	public void writeDouble(double x) {
		writeLong(Double.doubleToRawLongBits(x));
	}

	public double readDouble() {
		long value = readLong();
		return Double.longBitsToDouble(value);
	}
	
	public BSONObject readBSONObject(){
		try {
			return DECODER.get().readObject(this.asInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void writeBSONObject(BSONObject object){
		BasicOutputBuffer buffer = new BasicOutputBuffer();
		ENCODER.get().set(buffer);
		ENCODER.get().encode(object);
		try {
			buffer.pipe(this.asOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static int POSITION = 12;
	public static int getOPMessageType(byte[] message){
		byte[] b = message; // a little bit optimization
		
		return (b[POSITION] & 0xff) | ((b[POSITION+1] & 0xff) << 8)
				| ((b[POSITION+2] & 0xff) << 16)
				| ((b[POSITION+3] & 0xff) << 24);
	}
	public static int getRequestId(byte[] message){
		byte[] b = message; // a little bit optimization
		return (b[4] & 0xff) | ((b[4+1] & 0xff) << 8)
				| ((b[4+2] & 0xff) << 16)
				| ((b[4+3] & 0xff) << 24);
	}
	
	public static int getResponseId(byte[] message){
		byte[] b = message; // a little bit optimization
		return (b[8] & 0xff) | ((b[8+1] & 0xff) << 8)
				| ((b[8+2] & 0xff) << 16)
				| ((b[8+3] & 0xff) << 24);
	}
}
