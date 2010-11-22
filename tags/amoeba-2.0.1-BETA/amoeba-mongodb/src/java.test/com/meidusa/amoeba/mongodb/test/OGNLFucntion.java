package com.meidusa.amoeba.mongodb.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ognl.Ognl;
import ognl.OgnlException;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.JSON;

import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.config.PropertyTransfer;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.net.packet.AbstractPacket;

public class OGNLFucntion {

	
	/**
	 * @param args
	 * @throws OgnlException 
	 * @throws IOException 
	 * @throws InvalidPropertiesFormatException 
	 */
	public static void main(String[] args) throws OgnlException, InvalidPropertiesFormatException, IOException {
		final Map ctx0 = new HashMap();
		BSONObject prev = new BasicBSONObject();
		prev.put("count", 12);
		ctx0.put("prev",prev);
		prev.put("myTime", 11);
		BSONObject obj = new BasicBSONObject();
		obj.put("count", 13);
		ctx0.put("obj",obj);

		System.out.println(Ognl.getValue("#prev['count'] =1 + #prev['count']", ctx0, new Object()));
		
		System.out.println(prev.get("count"));
	}

}
