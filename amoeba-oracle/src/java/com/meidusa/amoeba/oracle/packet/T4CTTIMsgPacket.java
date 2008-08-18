package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

public abstract class T4CTTIMsgPacket extends DataPacket {

    protected byte msgCode;

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer packetBuffer = (T4CPacketBuffer) buffer;
        msgCode = (byte) packetBuffer.unmarshalUB1();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        buffer.writeByte(msgCode);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

    static final byte  TTIPRO                 = 1;
    static final byte  TTIDTY                 = 2;
    static final byte  TTIFUN                 = 3;
    static final byte  TTIOER                 = 4;
    static final byte  TTIRXH                 = 6;
    static final byte  TTIRXD                 = 7;
    static final byte  TTIRPA                 = 8;
    static final byte  TTISTA                 = 9;
    static final byte  TTIIOV                 = 11;
    static final byte  TTIUDS                 = 12;
    static final byte  TTIOAC                 = 13;
    static final byte  TTILOBD                = 14;
    static final byte  TTIWRN                 = 15;
    static final byte  TTIDCB                 = 16;
    static final byte  TTIPFN                 = 17;
    static final byte  TTIFOB                 = 19;
    static final byte  TTINTY                 = 1;
    static final byte  TTIBVC                 = 21;
    static final byte  OERFSPND               = 1;
    static final byte  OERFATAL               = 2;
    static final byte  OERFPLSW               = 4;
    static final byte  OERFUPD                = 8;
    static final byte  OERFEXIT               = 16;
    static final byte  OERFNCF                = 32;
    static final byte  OERFRDONLY             = 64;
    static final short OERFSBRK               = 128;
    static final byte  OERwANY                = 1;
    static final byte  OERwTRUN               = 2;
    static final byte  OERwLICM               = 2;
    static final byte  OERwNVIC               = 4;
    static final byte  OERwITCE               = 8;
    static final byte  OERwUDnW               = 16;
    static final byte  OERwCPER               = 32;
    static final byte  OERwPLEX               = 64;
    static final short ORACLE8_PROD_VERSION   = 8030;
    static final short ORACLE81_PROD_VERSION  = 8100;
    static final short MIN_OVERSION_SUPPORTED = 7230;
    static final short MIN_TTCVER_SUPPORTED   = 4;
    static final short V8_TTCVER_SUPPORTED    = 5;
    static final short MAX_TTCVER_SUPPORTED   = 6;
    static final int   REFCURSOR_SIZE         = 5;

}
