package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public abstract class T4CTTIMsgPacket extends DataPacket {

    public static final byte  TTIPRO                 = 1;
    public static final byte  TTIDTY                 = 2;
    public static final byte  TTIFUN                 = 3;
    public static final byte  TTIOER                 = 4;
    public static final byte  TTIRXH                 = 6;
    public static final byte  TTIRXD                 = 7;
    public static final byte  TTIRPA                 = 8;
    public static final byte  TTISTA                 = 9;
    public static final byte  TTIIOV                 = 11;
    public static final byte  TTIUDS                 = 12;
    public static final byte  TTIOAC                 = 13;
    public static final byte  TTILOBD                = 14;
    public static final byte  TTIWRN                 = 15;
    public static final byte  TTIDCB                 = 16;
    public static final byte  TTIPFN                 = 17;
    public static final byte  TTIFOB                 = 19;
    public static final byte  TTINTY                 = 1;
    public static final byte  TTIBVC                 = 21;
    public static final byte  OERFSPND               = 1;
    public static final byte  OERFATAL               = 2;
    public static final byte  OERFPLSW               = 4;
    public static final byte  OERFUPD                = 8;
    public static final byte  OERFEXIT               = 16;
    public static final byte  OERFNCF                = 32;
    public static final byte  OERFRDONLY             = 64;
    public static final short OERFSBRK               = 128;
    public static final byte  OERwANY                = 1;
    public static final byte  OERwTRUN               = 2;
    public static final byte  OERwLICM               = 2;
    public static final byte  OERwNVIC               = 4;
    public static final byte  OERwITCE               = 8;
    public static final byte  OERwUDnW               = 16;
    public static final byte  OERwCPER               = 32;
    static final byte         OERwPLEX               = 64;

    static final short        ORACLE8_PROD_VERSION   = 8030;
    static final short        ORACLE81_PROD_VERSION  = 8100;
    static final short        MIN_OVERSION_SUPPORTED = 7230;
    static final short        MIN_TTCVER_SUPPORTED   = 4;
    static final short        V8_TTCVER_SUPPORTED    = 5;
    static final short        MAX_TTCVER_SUPPORTED   = 6;
    static final int          REFCURSOR_SIZE         = 5;

    protected byte            msgCode;

    public T4CTTIMsgPacket(byte msgCode){
        this.msgCode = msgCode;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        unmarshal(buffer);
    }

    /**
     * 不包含头的消息解析
     */
    protected void unmarshal(AbstractPacketBuffer buffer) {
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        msgCode = (byte) meg.unmarshalUB1();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        marshal(buffer);
    }

    /**
     * 不包含头的消息封装
     */
    protected void marshal(AbstractPacketBuffer buffer) {
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalUB1(msgCode);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

    public static boolean isMsgType(byte[] buffer, byte type) {
        if (buffer.length > 11) {
            return (buffer[10] & 0xff) == (type & 0xff);
        } else {
            return false;
        }
    }

}
