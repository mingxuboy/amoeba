package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:19:24
 */
public class T4CTTIoexecDataPacket extends T4CTTIfunPacket {

    public T4CTTIoexecDataPacket(){
        this.funCode = OEXEC;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        // TODO Auto-generated method stub
        super.init(buffer);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        super.write2Buffer(buffer);
    }

}
