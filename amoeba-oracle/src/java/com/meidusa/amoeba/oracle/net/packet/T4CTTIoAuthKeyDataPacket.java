package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public class T4CTTIoAuthKeyDataPacket extends T4CTTIfunPacket {

    int      userLength = 0;
    long     LOGON_MODE = 0L;
    int      propLen    = 0;
    byte[]   user       = null;
    byte[][] keys       = null;
    byte[][] values     = null;

    public T4CTTIoAuthKeyDataPacket(){
        this.funCode = OSESSKEY;
    }

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
        propLen = (int) meg.unmarshalUB4();// key-value length
        meg.unmarshalUB1();
        meg.unmarshalUB1();
        user = meg.unmarshalCHR(userLength);

        keys = new byte[propLen][];
        values = new byte[propLen][];
        meg.unmarshalKEYVAL(keys, values, propLen);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalPTR();
        meg.marshalSB4(user.length);
        meg.marshalUB4(LOGON_MODE | 1L);
        meg.marshalPTR();
        meg.marshalUB4(propLen);
        meg.marshalPTR();
        meg.marshalPTR();
        meg.marshalCHR(user);
        byte[] abyte2 = new byte[propLen];
        meg.marshalKEYVAL(keys, values, abyte2, propLen);
    }

}
