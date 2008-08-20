package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.util.DBConversion;

/**
 * 协议数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:29:53
 */
public class T4C8TTIproResponseDataPacket extends T4CTTIMsgPacket {

    byte           proSvrVer        = 6;
    short          oVersion         = -1;
    String         proSvrStr        = "Linuxi386/Linux-2.0.34-8.1.0";
    short          svrCharSet       = 0;
    byte           svrFlags         = 1;
    short          svrCharSetElem   = 0;
    boolean        svrInfoAvailable = false;
    short          NCHAR_CHARSET    = 0;

    private int    i                = 0;
    private byte[] abyte0           = null;
    private short  word0            = 0;
    private byte[] as0              = null;
    private short  word1            = 0;
    private byte[] as1              = null;

    public T4C8TTIproResponseDataPacket(){
        this.msgCode = TTIPRO;
    }

    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        if (msgCode != TTIPRO) {
            throw new RuntimeException("违反协议");
        }
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        proSvrVer = meg.unmarshalSB1();
        switch (proSvrVer) {
            case 4:
                oVersion = MIN_OVERSION_SUPPORTED;
                break;
            case 5:
                oVersion = ORACLE8_PROD_VERSION;
                break;
            case 6:
                oVersion = ORACLE81_PROD_VERSION;
                break;
            default:
                throw new RuntimeException("不支持从服务器接收到的 TTC 协议版本");
        }
        meg.unmarshalSB1();
        proSvrStr = new String(meg.unmarshalTEXT(50));
        svrCharSet = (short) meg.unmarshalUB2();
        svrFlags = (byte) meg.unmarshalUB1();
        svrCharSetElem = (short) meg.unmarshalUB2();
        if (svrCharSetElem > 0) {
            meg.unmarshalNBytes(svrCharSetElem * 5);
        }
        svrInfoAvailable = true;

        if (proSvrVer < 5) {
            return;
        }
        byte byte0 = meg.getTypeRep().getRep((byte) 1);
        meg.getTypeRep().setRep((byte) 1, (byte) 0);
        i = meg.unmarshalUB2();
        meg.getTypeRep().setRep((byte) 1, byte0);
        abyte0 = meg.unmarshalNBytes(i);
        int j = 6 + (abyte0[5] & 0xff) + (abyte0[6] & 0xff);
        NCHAR_CHARSET = (short) ((abyte0[j + 3] & 0xff) << 8);
        NCHAR_CHARSET |= (short) (abyte0[j + 4] & 0xff);

        if (proSvrVer < 6) {
            return;
        }
        word0 = meg.unmarshalUB1();
        as0 = new byte[word0];
        for (int k = 0; k < word0; k++) {
            as0[k] = (byte) meg.unmarshalUB1();
        }
        word1 = meg.unmarshalUB1();
        as1 = new byte[word1];
        for (int l = 0; l < word1; l++) {
            as1[l] = (byte) meg.unmarshalUB1();
        }
        short word0 = oVersion;
        short word1 = svrCharSet;
        short word2 = DBConversion.findDriverCharSet(word1, word0);

        try {
            DBConversion conversion = new DBConversion(word1, word2, NCHAR_CHARSET);
            meg.setConversion(conversion);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        meg.getTypeRep().setServerConversion(word2 != word1);
        meg.getTypeRep().setVersion(word0);

    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.writeByte(proSvrVer);
        meg.marshalNULLPTR();
        meg.writeBytes(proSvrStr.getBytes());
        meg.marshalNULLPTR();
        meg.marshalUB2(svrCharSet);
        meg.marshalUB1(svrFlags);
        meg.marshalUB2(svrCharSetElem);
        if (svrCharSetElem > 0) {
            byte[] ab = new byte[svrCharSetElem * 5];
            meg.marshalB1Array(ab);
        }

        if (proSvrVer < 5) {
            return;
        }
        byte byte0 = meg.getTypeRep().getRep((byte) 1);
        meg.getTypeRep().setRep((byte) 1, (byte) 0);
        meg.marshalUB2(i);
        meg.getTypeRep().setRep((byte) 1, byte0);
        meg.marshalB1Array(abyte0);

        if (proSvrVer < 6) {
            return;
        }
        meg.marshalUB1(word0);
        meg.marshalB1Array(as0);
        meg.marshalUB1(word1);
        meg.marshalB1Array(as1);
    }

}
