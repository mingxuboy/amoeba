package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public class T4CTTIofetchDataPacket extends T4CTTIfunPacket {

    public T4CTTIofetchDataPacket(){
        this.funCode = OFETCH;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
    }

}
