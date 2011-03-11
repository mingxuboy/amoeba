package com.meidusa.amoeba.mongodb.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ognl.OgnlException;

import org.bson.BSONObject;
import org.bson.JSON;

import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.config.PropertyTransfer;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.net.packet.AbstractPacket;

public class TestOGNL {

	public static AbstractMongodbPacket createRequestPacket(Properties properties,Map map) {
		AbstractMongodbPacket packet = null;
		try {
			packet = (AbstractMongodbPacket)Class.forName((String)properties.get("class")).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		ParameterMapping.mappingObjectField(packet, properties,map,new Object(), AbstractPacket.class);
		return packet;
	}
	/**
	 * @param args
	 * @throws OgnlException 
	 * @throws IOException 
	 * @throws InvalidPropertiesFormatException 
	 */
	public static void main(String[] args) throws OgnlException, InvalidPropertiesFormatException, IOException {
		final Map ctx0 = new HashMap();
		Random random = new Random();
		ctx0.put("random",random);
		ctx0.put("atomicInteger",new AtomicInteger());
		ctx0.put("atomicLong",new AtomicLong());
		ctx0.put("aa.dd","asdfadf");
		//ctx0.put("math",Math.class);
		ctx0.put("math",new Object(){
			public long abs(long value){
				return Math.abs(value);
			}
			
			public int abs(int value){
				return Math.abs(value);
			}
		});
		
		ParameterMapping.registerTransfer(BSONObject.class, new PropertyTransfer<BSONObject>(){
			@Override
			public BSONObject transfer(String inputString) {
				return (BSONObject)JSON.parse(ConfigUtil.filterWtihOGNL(inputString, ctx0));
			}
		});
		
		Properties properties = new Properties();
		properties.loadFromXML(TestOGNL.class.getResourceAsStream("Ognl.xml"));

		Object root = new Object();
		
		createRequestPacket(properties,ctx0);
		System.out.println(createRequestPacket(properties,ctx0));
		
		long start= System.currentTimeMillis();
		for(int i=0;i<100000;i++){
			createRequestPacket(properties,ctx0);
		}
		System.out.println((System.currentTimeMillis() - start));
	}

}
