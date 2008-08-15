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
public class AbstractPacket implements Packet, OraclePacketConstant {

    private byte[]  buffer;
    protected int   length;
    protected short type;
    protected short flags;
    protected int   dataLen;
    protected int   dataOffset;
    protected int   packetCheckSum;
    protected int   headerCheckSum;

    public void init(byte[] buffer) {
        this.buffer = buffer;
        init(new AnoPacketBuffer(buffer));
    }

    protected void init(AnoPacketBuffer buffer) {
        length = buffer.readUB2();
        packetCheckSum = buffer.readUB2();
        type = buffer.readUB1();
        flags = buffer.readUB1();
        headerCheckSum = buffer.readUB2();
    }

    /**
     * 将数据包转化成ByteBuffer
     */
    public ByteBuffer toByteBuffer() {
        try {
            return toBuffer().toByteBuffer();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * 写完之后一定需要调用这个方法，buffer的指针位置指向末尾的下一个位置（包总长度位置）。
     */
    protected void afterPacketWritten(AnoPacketBuffer buffer) {
        int position = buffer.getPosition();
        int packetLength = position;
        buffer.setPosition(0);
        buffer.writeUB2(packetLength);
        buffer.writeUB2(packetCheckSum);
        buffer.writeUB1(type);
        buffer.writeUB1(flags);
        buffer.writeUB2(headerCheckSum);
        buffer.setPosition(position);
        buffer.setPacketLength(packetLength);
    }

    /**
     * @param buffer 用于输入输出的缓冲
     * @throws UnsupportedEncodingException 当String to bytes发生编码不支持的时候
     */
    protected void write2Buffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
        buffer.setPosition(HEADER_SIZE);
    }

    /**
     * 该方法调用了{@link #write2Buffer(PacketBuffer)} 写入到指定的buffer，并且调用了{@link #afterPacketWritten(PacketBuffer)}
     */
    protected AnoPacketBuffer toBuffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
        write2Buffer(buffer);
        afterPacketWritten(buffer);
        return buffer;
    }

    protected AnoPacketBuffer toBuffer() throws UnsupportedEncodingException {
        int bufferSize = calculatePacketSize();
        bufferSize = (bufferSize < (DATA_OFFSET + 1) ? (DATA_OFFSET + 1) : bufferSize);
        AnoPacketBuffer buffer = new AnoPacketBuffer(bufferSize);
        return toBuffer(buffer);
    }

    /**
     * 估算packet的大小，估算的太大浪费内存，估算的太小会影响性能
     */
    protected int calculatePacketSize() {
        return DATA_OFFSET + 1;
    }

    protected String extractData() {
        String data;
        if (dataLen <= 0) data = new String();
        else if (length > dataOffset) {
            data = new String(buffer, dataOffset, dataLen);
        } else {
            byte abyte0[] = new byte[dataLen];
            data = new String(abyte0);
        }
        return data;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Object clone() {
        try {
            return (AbstractPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            // 逻辑上面不会发生不支持情况
            return null;
        }
    }

}
