package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-21 ÏÂÎç06:28:49
 */
public class T4C8OcloseDataPacket extends T4CTTIfunPacket {

    public T4C8OcloseDataPacket(){
        super(TTIPFN, OCANA, (byte) 0);
    }

    @Override
    protected void marshal(AbstractPacketBuffer buffer) {
        super.marshal(buffer);
    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);
    }

    void initCloseQuery() {
        this.funCode = OCANA;
    }

    void initCloseStatement() {
        this.funCode = OCCA;
    }

}
