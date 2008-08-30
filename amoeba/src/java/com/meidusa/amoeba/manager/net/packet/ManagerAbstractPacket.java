package com.meidusa.amoeba.manager.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacket;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * 
 * @author struct
 *
 */
public class ManagerAbstractPacket extends AbstractPacket {

	@Override
	protected void afterPacketWritten(AbstractPacketBuffer buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	protected int calculatePacketSize() {
		return 12;
	}

	@Override
	protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
		return ManagerPacketBuffer.class;
	}

	@Override
	protected void init(AbstractPacketBuffer buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void write2Buffer(AbstractPacketBuffer buffer)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

}
