package com.meidusa.amoeba.manager.test;

import java.nio.ByteBuffer;

import com.meidusa.amoeba.manager.net.packet.ObjectPacket;

public class TestObjectPacket {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ObjectPacket objectpacket = new ObjectPacket();
		objectpacket.object = "hello world!!!";
		objectpacket.funType = ObjectPacket.FUN_TYPE_OBJECT;
		ByteBuffer buffer = objectpacket.toByteBuffer(null);
		
		ObjectPacket newpacket = new ObjectPacket();
		newpacket.init(buffer.array(), null);
		System.out.println(newpacket);
	}

}
