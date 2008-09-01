package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
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
public abstract class AbstractPacket extends com.meidusa.amoeba.net.packet.AbstractPacket implements SQLnetDef, OraclePacketConstant {

    protected int   length;
    protected short type;
    protected short flags;
    protected int   dataLen;
    protected int   dataOffset;
    protected int   packetCheckSum;
    protected int   headerCheckSum;
    private String  data;
    private byte[]  buffer;

    public AbstractPacket(short type){
        this.type = type;
    }

    public void init(byte[] buffer, Connection conn) {
        this.buffer = buffer;
        super.init(buffer, conn);
    }

    /**
     * 包含头的消息解析
     */
    protected void init(AbstractPacketBuffer buffer) {
        OracleAbstractPacketBuffer oracleBuffer = (OracleAbstractPacketBuffer) buffer;
        length = oracleBuffer.readUB2();
        packetCheckSum = oracleBuffer.readUB2();
        type = oracleBuffer.readUB1();
        flags = oracleBuffer.readUB1();
        headerCheckSum = oracleBuffer.readUB2();
    }

    /**
     * 包含头的消息封装
     */
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        buffer.setPosition(HEADER_SIZE);
    }

    /**
     * 写完之后一定需要调用这个方法，buffer的指针位置指向末尾的下一个位置（包总长度位置）。
     */
    protected void afterPacketWritten(AbstractPacketBuffer buffer) {
        int position = buffer.getPosition();
        buffer.setPosition(0);
        OracleAbstractPacketBuffer oracleBuffer = (OracleAbstractPacketBuffer) buffer;
        oracleBuffer.writeUB2(position);
        oracleBuffer.writeUB2(packetCheckSum);
        oracleBuffer.writeUB1(type);
        oracleBuffer.writeUB1(flags);
        oracleBuffer.writeUB2(headerCheckSum);
        buffer.setPosition(position);
    }

    /**
     * 估算packet的大小，估算的太大浪费内存，估算的太小会影响性能
     */
    protected int calculatePacketSize() {
        return DATA_OFFSET + 1;
    }

    protected String extractData() {
        if (dataLen <= 0) {
            data = new String();
        } else if (length > dataOffset) {
            data = new String(buffer, dataOffset, dataLen);
        } else {
            byte abyte0[] = new byte[dataLen];
            data = new String(abyte0);
        }
        return data;
    }

    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return OracleAbstractPacketBuffer.class;
    }

    public String getData() {
        return data;
    }

}
