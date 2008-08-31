package com.meidusa.amoeba.manager.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public class ObjectPacket extends ManagerAbstractPacket {
	public Object object;
	
	protected void init(AbstractPacketBuffer buffer) {
		super.init(buffer);
		ManagerPacketBuffer mBuffer = (ManagerPacketBuffer)buffer;
		object = mBuffer.readObject();
	}
	
	@Override
	protected void write2Buffer(AbstractPacketBuffer buffer)
			throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		ManagerPacketBuffer mBuffer = (ManagerPacketBuffer)buffer;
		mBuffer.writeObject(object);
	}
}
