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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class AbstractPacket implements Packet {
	protected static Logger logger = Logger.getLogger(AbstractPacket.class);
	/**
	 * 只表示数据长度，不包含包头长度
	 */
	public int packetLength;
	
	/**
	 * 当前的数据包序列数
	 */
	public byte packetId;
	
	/**
	 * 从buffer(含包头) 中初始化数据包。
	 * @param buffer buffer是从mysql socketChannel的流读取头4个字节计算数据包长度
	 * 				并且读取相应的长度所形成的buffer
	 */
	public void init(byte[] buffer,Connection conn){
		MysqlPacketBuffer packetBuffer = new MysqlPacketBuffer(buffer);
		packetBuffer.init(conn);
		init(packetBuffer);
	}
	
	/**
	 * 将数据包转化成ByteBuffer
	 * @return
	 */
	public ByteBuffer toByteBuffer(Connection conn){
		try {
			return toBuffer(conn).toByteBuffer();
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public void init(MysqlPacketBuffer buffer) {
		buffer.setPosition(0);
		packetLength = (buffer.readByte() & 0xff)	
		+ ((buffer.readByte() & 0xff) << 8)	
		+ ((buffer.readByte() & 0xff) << 16);
		packetId = buffer.readByte();
	}

	/**
	 * 写完之后一定需要调用这个方法，buffer的指针位置指向末尾的下一个位置（包总长度位置）。
	 * @param buffer
	 */
	protected void afterPacketWritten(MysqlPacketBuffer buffer){
		int position = buffer.getPosition();
		packetLength = position-HEADER_SIZE;
		buffer.setPosition(0);
		buffer.writeByte((byte)(packetLength & 0xff));
		buffer.writeByte((byte) (packetLength >>> 8));
		buffer.writeByte((byte) (packetLength >>> 16));
		buffer.writeByte((byte) packetId);// packet id
		buffer.setPosition(position);
	}
	
	/**
	 * 将该packet写入到buffer中，该buffer中包含4个字节的包头，写完以后将计算buffer包头值
	 * @param buffer 用于输入输出的缓冲
	 * @throws UnsupportedEncodingException 当String to bytes发生编码不支持的时候
	 */
	protected void write2Buffer(MysqlPacketBuffer buffer) throws UnsupportedEncodingException {
		
	}

	/**
	 * 该方法调用了{@link #write2Buffer(MysqlPacketBuffer)} 写入到指定的buffer，并且调用了{@link #afterPacketWritten(MysqlPacketBuffer)}
	 */
	public MysqlPacketBuffer toBuffer(MysqlPacketBuffer buffer) throws UnsupportedEncodingException {
		write2Buffer(buffer);
		afterPacketWritten(buffer);
		return buffer;
	}
	
	private MysqlPacketBuffer toBuffer(Connection conn) throws UnsupportedEncodingException{
		int bufferSize = calculatePacketSize();
		bufferSize = (bufferSize<5?5:bufferSize);
		MysqlPacketBuffer buffer = new MysqlPacketBuffer(bufferSize);
		buffer.init(conn);
		return toBuffer(buffer);
	}
	
	/**
	 * 估算packet的大小，估算的太大浪费内存，估算的太小会影响性能
	 * @return
	 */
	protected int calculatePacketSize(){
		return HEADER_SIZE + 1;
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}
	
	
	public  Object clone(){
		try {
			return (AbstractPacket)super.clone();
		} catch (CloneNotSupportedException e) {
			//逻辑上面不会发生不支持情况
			return null;
		}
	}
}
