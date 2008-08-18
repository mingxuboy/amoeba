package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

public abstract class T4CTTIMsgDataPacket extends DataPacket implements T4CTTIMsg {

    protected byte msgCode;

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer packetBuffer = (T4CPacketBuffer) buffer;
        msgCode = (byte) packetBuffer.unmarshalUB1();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        buffer.writeByte(msgCode);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }
    
}
