package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.meidusa.amoeba.oracle.io.OraclePacketConstant;

/**
 * <pre>
 * 
 * |-------------------------------------------
 * |Common Packet Header  | 8   | 通用包头
 * |--------------------------------------------
 * |                Data  |可变 | 数据
 * |--------------------------------------------
 * |
 * |
 * |通用包头格式  每个TNS完整数据都包含一个通用包头
 * |他说明接受数据的长度及其相关校验和解析的信息。
 * |------------------------------------------------
 * |              Length  | 2   | 包的长度，包括通用包头
 * |--------------------------------------------------
 * |    Packet check sum  | 2   | 包的校验和
 * |------------------------------------------------
 * |                Type  | 1   | TNS 包类型
 * |-----------------------------------------------
 * |                Flag  | 1   | 状态
 * |----------------------------------------------
 * |    Header check sum  | 2   | 通用头的校验和
 * |---------------------------------------------
 * 
 * 
 * </pre>
 * 
 * @author struct
 */
public class AbstractPacket implements Packet,OraclePacketConstant {

    protected int  length;
    protected byte type;
    protected byte flags;
    protected int  dataLen;
    protected int  dataOff;
    protected int  packetCheckSum;
    protected int  headerCheckSum;

    public void init(byte[] buffer) {
        init(new AnoPacketBuffer(buffer));
    }
    
    protected void init(AnoPacketBuffer buffer){
    	length = buffer.readUB2();
    	packetCheckSum = buffer.readUB2();
    	type = (byte)(buffer.readUB1()& 0xff);
    	flags = (byte)(buffer.readUB1()& 0xff);
    	headerCheckSum = buffer.readUB2();
    }

    /**
	 * 将数据包转化成ByteBuffer
	 * @return
	 */
	public ByteBuffer toByteBuffer(){
		try {
			return toBuffer().toByteBuffer();
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

    /**
	 * 写完之后一定需要调用这个方法，buffer的指针位置指向末尾的下一个位置（包总长度位置）。
	 * @param buffer
	 */
	protected void afterPacketWritten(AnoPacketBuffer buffer){
		int position = buffer.getPosition();
		int packetLength = position;
		buffer.setPosition(0);
		buffer.writeByte((byte)(packetLength/ 256));
		buffer.writeByte((byte)(packetLength % 256));
		buffer.setPosition(4);
		buffer.writeByte(type);
		buffer.writeByte(flags);
		buffer.setPosition(position);
	}
	
	/**
	 * @param buffer 用于输入输出的缓冲
	 * @throws UnsupportedEncodingException 当String to bytes发生编码不支持的时候
	 */
	protected void write2Buffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
		buffer.writeUB2(length);
		buffer.writeUB2(packetCheckSum);
		buffer.writeUB1(type);
		buffer.writeUB1(flags);
		buffer.writeUB2(headerCheckSum);
	}

	/**
	 * 该方法调用了{@link #write2Buffer(PacketBuffer)} 写入到指定的buffer，并且调用了{@link #afterPacketWritten(PacketBuffer)}
	 */
	public AnoPacketBuffer toBuffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
		write2Buffer(buffer);
		afterPacketWritten(buffer);
		return buffer;
	}
	
	public AnoPacketBuffer toBuffer() throws UnsupportedEncodingException{
		int bufferSize = calculatePacketSize();
		bufferSize = (bufferSize<5?5:bufferSize);
		AnoPacketBuffer buffer = new AnoPacketBuffer(bufferSize);
		return toBuffer(buffer);
	}
	
	/**
	 * 估算packet的大小，估算的太大浪费内存，估算的太小会影响性能
	 * @return
	 */
	protected int calculatePacketSize(){
		return DATA_OFFSET + 1;
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
