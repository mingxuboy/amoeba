package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;


public class T4CTTIfunPacket extends T4CTTIMsgPacket {
	public short                       funCode;
	public byte                        seqNumber;
    
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
        funCode = meg.unmarshalUB1();
        seqNumber = (byte)meg.unmarshalUB1();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
        meg.marshalUB1(funCode);
        meg.marshalUB1(seqNumber);
    }
    
    public static boolean isFunType(byte[] buffer,short type){
    	if(buffer == null || buffer.length<=12){
    		return false;
    	}else{
    		return isMsgType(buffer,TTIFUN) && ((buffer[11] & 0xff) == (type & 0xff));
    	}
    }
    
    public static final short          OOPEN         = 2;
    public static final short          OEXEC         = 4;
    public static final short          OFETCH        = 5;
    public static final short          OCLOSE        = 8;
    public static final short          OLOGOFF       = 9;
    public static final short          OCOMON        = 12;
    public static final short          OCOMOFF       = 13;
    public static final short          OCOMMIT       = 14;
    public static final short          OROLLBACK     = 15;
    public static final short          OCANCEL       = 20;
    public static final short          ODSCRARR      = 43;
    public static final short          OVERSION      = 59;
    public static final short          OK2RPC        = 67;
    public static final short          OALL7         = 71;
    public static final short          OSQL7         = 74;
    public static final short          O3LOGON       = 81;
    public static final short          O3LOGA        = 82;
    public static final short          OKOD          = 92;
    public static final short          OALL8         = 94;
    public static final short          OLOBOPS       = 96;
    public static final short          ODNY          = 98;
    public static final short          OTXSE         = 103;
    public static final short          OTXEN         = 104;
    public static final short          OCCA          = 105;
    public static final short          O80SES        = 107;
    public static final short          OAUTH         = 115;
    public static final short          OSESSKEY      = 118;
    public static final short          OCANA         = 120;
    public static final short          OOTCM         = 127;
    public static final short          OSCID         = 135;
    public static final short          OKPFC         = 139;
    public static final short          OKEYVAL       = 154;
}
