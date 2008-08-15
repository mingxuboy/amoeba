package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * 协议数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:29:53
 */
public class T4C8TTIproDataPacket extends T4CTTIMsgDataPacket {

    byte    proSvrVer;
    short   svrCharSet;
    short   svrCharSetElem;
    byte    svrFlags;
    byte[]  proSvrStr;
    short   oVersion         = -1;
    boolean svrInfoAvailable = false;
    byte[]  proCliVerTTC8    = { 6, 5, 4, 3, 2, 1, 0 };
    byte[]  proCliStrTTC8    = { 74, 97, 118, 97, 95, 84, 84, 67, 45, 56, 46, 50, 46, 48, 0 };
    short   NCHAR_CHARSET    = 0;

    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        if (msgCode != TTIPRO) {
            throw new RuntimeException("违反协议");
        }
        T4CPacketBuffer buffer = (T4CPacketBuffer) absbuffer;
        proSvrVer = buffer.unmarshalSB1();// protocol version
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
        buffer.unmarshalSB1();// check flag
        proSvrStr = buffer.unmarshalTEXT(50);

    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        T4CPacketBuffer buffer = (T4CPacketBuffer) absbuffer;

    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

}
