package com.meidusa.amoeba.mysql.test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.meidusa.amoeba.mysql.net.packet.AbstractPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.util.StringUtil;

public class TestPacket extends AbstractPacket {
	public String data; 
	
	public void init(MysqlPacketBuffer buffer) {
		super.init(buffer);
		data = buffer.readString();
	}
	protected void write2Buffer(MysqlPacketBuffer buffer) throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeString(data);
	}
	
	public static void main(String[] args){
		TestPacket packet = new TestPacket();
		packet.packetId = 13;
		packet.data = "asdfasdddddddddddddddddddddddddddddddddd1234567890";
		
		byte[] buffer = packet.toByteBuffer(null).array();
		System.out.println(StringUtil.dumpAsHex(buffer,buffer.length));
		TestPacket newpacket = new TestPacket();
		newpacket.init(buffer, null);
		System.out.println(newpacket);
		
		ByteBuffer bytebuffer = packet.toByteBuffer(null);
		System.out.println(bytebuffer);
		System.out.println("remain:"+bytebuffer.remaining());
		bytebuffer.get(new byte[13]);
		System.out.println(bytebuffer.position());
		System.out.println("remain:"+bytebuffer.remaining());
	}
}
