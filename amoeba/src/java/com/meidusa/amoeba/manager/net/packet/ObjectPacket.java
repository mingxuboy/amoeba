package com.meidusa.amoeba.manager.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacket;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public class ObjectPacket extends AbstractPacket {
	public Object object;

	@Override
	protected void afterPacketWritten(AbstractPacketBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int calculatePacketSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
		// TODO Auto-generated method stub
		return null;
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
