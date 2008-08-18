package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * 协议数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:29:53
 */
public class T4C8TTIproDataPacket extends T4CTTIMsgPacket {

    byte[] proCliVerTTC8 = { 6, 5, 4, 3, 2, 1 };
    String proCliStrTTC8 = "Java_TTC-8.2.0";

    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        // if (msgCode != TTIPRO) {
        // throw new RuntimeException("违反协议");
        // }
        // T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        // meg.readBytes(proCliVerTTC8, 0, proCliVerTTC8.length);
        // meg.readBytes(proCliStrTTC8, 0, proCliStrTTC8.length);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        msgCode = TTIPRO;
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.writeBytes(proCliVerTTC8);
        meg.writeByte((byte) 0);
        meg.writeBytes(proCliStrTTC8.getBytes());
        meg.writeByte((byte) 0);
    }

}
