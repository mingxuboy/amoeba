package com.meidusa.amoeba.mongodb.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import com.meidusa.amoeba.config.ConfigUtil;


import ognl.Ognl;
import ognl.OgnlException;

public class TestOGNL {

	/**
	 * @param args
	 * @throws OgnlException 
	 * @throws IOException 
	 * @throws InvalidPropertiesFormatException 
	 */
	public static void main(String[] args) throws OgnlException, InvalidPropertiesFormatException, IOException {
		Map ctx0 = new HashMap();
		Random random = new Random();
		ctx0.put("v1","length()");
		ctx0.put("random",random);
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
		
		Properties properties = new Properties();
		properties.loadFromXML(TestOGNL.class.getResourceAsStream("Ognl.xml"));

		Object root = new Object();
		String selector = properties.getProperty("selector").trim();
		Map<String,Object> expression = ConfigUtil.preparedOGNL(selector);
		System.out.println(ConfigUtil.filterWtihOGNL(selector, expression, ctx0));
		
		long start= System.currentTimeMillis();
		for(int i=0;i<100000;i++){
			ConfigUtil.filterWtihOGNL(selector, expression, ctx0);
		}
		System.out.println((System.currentTimeMillis() - start));
	}

}
