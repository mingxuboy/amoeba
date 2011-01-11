package com.meidusa.amoeba.manager.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.manager.ManagerConstant;
import com.meidusa.amoeba.net.packet.AbstractPacket;

/**
 * @author struct
 */
public class ManagerAbstractPacket extends AbstractPacket<ManagerPacketBuffer> implements ManagerConstant {

    public int  lenght;
    public byte funType;

    @Override
    protected void init(ManagerPacketBuffer buffer) {
        buffer.setPosition(0);
        lenght = buffer.readInt();
        funType = buffer.readByte();
    }

    @Override
    protected void write2Buffer(ManagerPacketBuffer buffer) throws UnsupportedEncodingException {
        buffer.setPosition(HEADER_SIZE);
    }

    @Override
    protected void afterPacketWritten(ManagerPacketBuffer buffer) {
        int position = buffer.getPosition();
        lenght = position;
        buffer.setPosition(0);
        buffer.writeInt(lenght);
        buffer.writeByte(funType);
        buffer.setPosition(position);
    }

    @Override
    protected int calculatePacketSize() {
        return 5;
    }


    @Override
    protected void afterRead(ManagerPacketBuffer buffer) {
        buffer.setPosition(HEADER_SIZE);
    }

	@Override
	protected Class<ManagerPacketBuffer> getPacketBufferClass() {
		return ManagerPacketBuffer.class;
	}
}
