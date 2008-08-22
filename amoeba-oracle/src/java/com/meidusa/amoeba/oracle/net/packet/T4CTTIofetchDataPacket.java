package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public class T4CTTIofetchDataPacket extends T4CTTIfunPacket {

    int cursor;
    int al8i4_1;

    public T4CTTIofetchDataPacket(){
        super(OFETCH);
    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        cursor = meg.unmarshalSWORD();
        al8i4_1 = meg.unmarshalSWORD();
    }

}
