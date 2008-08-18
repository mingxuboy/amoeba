package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

public class T4CTTIoAuthKeyDataPacket extends T4CTTIfunPacket {

    int    userLength = 0;
    long   LOGON_MODE = 0L;
    byte[] user;

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        if (funCode != OSESSKEY) {
            throw new RuntimeException("Œ•∑¥–≠“È");
        }
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.unmarshalUB1();
        userLength = meg.unmarshalSB4();
        LOGON_MODE = meg.unmarshalUB4();
        meg.unmarshalUB1();
        int l1 = (int)meg.unmarshalUB4();//programName len
        meg.unmarshalUB1();
        meg.unmarshalUB1();
        user = meg.unmarshalCHR(userLength);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        setHeader();
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalPTR();
    }

    protected void setHeader() {
        this.funCode = OSESSKEY;
        this.seqNumber = 0;
    }

}
