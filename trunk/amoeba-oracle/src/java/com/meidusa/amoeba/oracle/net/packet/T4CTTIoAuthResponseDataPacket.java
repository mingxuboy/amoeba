package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-19 ÏÂÎç02:43:18
 */
public class T4CTTIoAuthResponseDataPacket extends DataPacket {

    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

}
