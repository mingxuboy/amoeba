package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

public class T4CTTIoAuthKeyDataPacket extends T4CTTIfunPacket {

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        setHeader();
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalPTR();
    }

    protected void setHeader() {
        this.msgCode = TTIFUN;
        this.funCode = OSESSKEY;
        this.seqNumber = 0;
    }

}
