package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * 协议数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:29:53
 */
public class T4C8TTIproDataPacket extends DataPacket implements T4CTTIMsg {

    byte  ttcCode;
    short oVersion;

    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        T4CPacketBuffer buffer = (T4CPacketBuffer) absbuffer;

        // length = buffer.unmarshalUB2();
        // packetCheckSum = buffer.readUB2();
        // type = buffer.readUB1();
        // flags = buffer.readUB1();
        // headerCheckSum = buffer.readUB2();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        T4CPacketBuffer buffer = (T4CPacketBuffer) absbuffer;
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

}
