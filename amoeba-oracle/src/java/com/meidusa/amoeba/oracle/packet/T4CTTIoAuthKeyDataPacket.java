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
        this.msgCode = TTIFUN;
        this.funCode = OSESSKEY;
        this.seqNumber = 0;
        super.write2Buffer(buffer);

    }
    
    protected void marshalFunHeader(){
        
    }

}
