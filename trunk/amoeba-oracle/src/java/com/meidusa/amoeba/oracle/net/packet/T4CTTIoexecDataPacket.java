package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 обнГ02:19:24
 */
public class T4CTTIoexecDataPacket extends T4CTTIfunPacket {

    int cursor;
    int al8i4_1;

    public T4CTTIoexecDataPacket(){
        super(OEXEC);
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        cursor = meg.unmarshalSWORD();
        al8i4_1 = meg.unmarshalSWORD();
        meg.unmarshalSWORD();
        meg.unmarshalSWORD();
    }

}
