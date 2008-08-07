package com.meidusa.amoeba.oracle.packet;

import java.nio.ByteBuffer;

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
public class AbstractPacket implements Packet {

    protected byte buffer[];
    protected int  length;
    protected byte type;
    protected byte flags;
    protected int  dataLen;
    protected int  dataOff;
    protected int  packetCheckSum;
    protected int  headerCheckSum;

    public void init(byte[] buffer) {
        this.buffer = buffer;
        length = buffer[0] & 0xff;
        length <<= 8;
        length |= buffer[1] & 0xff;
        type = buffer[4];
        flags = buffer[5];
    }

    public ByteBuffer toByteBuffer() {
        if (buffer == null) {

        }
        return null;
    }

}
