package com.meidusa.amoeba.manager.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacket;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * 
 * @author struct
 *
 */
public class ManagerAbstractPacket extends AbstractPacket implements PacketConstant{
	
	public int lenght;
	public byte funType;
	
	@Override
	protected void init(AbstractPacketBuffer buffer) {
		buffer.setPosition(0);
		lenght = (buffer.readByte() & 0xff)	
		+ ((buffer.readByte() & 0xff) << 8)	
		+ ((buffer.readByte() & 0xff) << 16);
		funType = buffer.readByte();
		buffer.setPosition(HEADER_SIZE);
	}

	@Override
	protected void write2Buffer(AbstractPacketBuffer buffer)
			throws UnsupportedEncodingException {
		buffer.setPosition(HEADER_SIZE);
	}
	
	@Override
	protected void afterPacketWritten(AbstractPacketBuffer buffer) {
		int position = buffer.getPosition();
		lenght = position-HEADER_SIZE;
		buffer.setPosition(0);
		buffer.writeByte((byte)(lenght & 0xff));
		buffer.writeByte((byte) (lenght >>> 8));
		buffer.writeByte((byte) (lenght >>> 16));
		buffer.writeByte((byte) funType);// packet id
		buffer.setPosition(position);
	}

	@Override
	protected int calculatePacketSize() {
		return 12;
	}

	@Override
	protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
		return ManagerPacketBuffer.class;
	}
}
