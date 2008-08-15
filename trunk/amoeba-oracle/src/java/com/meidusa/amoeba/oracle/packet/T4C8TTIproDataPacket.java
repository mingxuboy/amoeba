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

    byte[] proCliVerTTC8 = { 6, 5, 4, 3, 2, 1, 0 };
    byte[] proCliStrTTC8 = { 74, 97, 118, 97, 95, 84, 84, 67, 45, 56, 46, 50, 46, 48, 0 };

    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        if (msgCode != TTIPRO) {
            throw new RuntimeException("违反协议");
        }
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.readBytes(proCliVerTTC8, 0, proCliVerTTC8.length);
        meg.readBytes(proCliStrTTC8, 0, proCliStrTTC8.length);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        msgCode = TTIPRO;
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.writeBytes(proCliVerTTC8);
        meg.writeBytes(proCliStrTTC8);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

}
