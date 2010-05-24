package com.meidusa.amoeba.manager.test;

import java.nio.ByteBuffer;

import com.meidusa.amoeba.config.BeanObjectEntityConfig;
import com.meidusa.amoeba.config.DBServerConfig;
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
		DBServerConfig serverConfig = new DBServerConfig();
		serverConfig.setName("hello");
		BeanObjectEntityConfig poolConfig = new BeanObjectEntityConfig();
		poolConfig.setName("poolconfig");
		poolConfig.setClassName("com.meidusa.amoeba.test");
		poolConfig.getParams().put("asdafd", "qwewerwer");
		serverConfig.setPoolConfig(poolConfig);
		objectpacket = new ObjectPacket();
		objectpacket.object = serverConfig;
		buffer = objectpacket.toByteBuffer(null);
		newpacket = new ObjectPacket();
		newpacket.init(buffer.array(), null);
		System.out.println(newpacket);
		
	}

}
