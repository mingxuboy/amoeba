package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

public abstract class T4CTTIMsgDataPacket extends DataPacket implements T4CTTIMsg {
	public byte msgCode;
	@Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        msgCode = (byte)(buffer.readByte() & 0xff);
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
}
