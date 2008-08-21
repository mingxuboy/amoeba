package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-21 обнГ06:28:49
 */
public class T4C8OcloseDataPacket extends T4CTTIfunPacket {

    public T4C8OcloseDataPacket(){
        super(TTIPFN, OCCA, (byte) 0);
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
    }

}
