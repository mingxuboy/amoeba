package com.meidusa.amoeba.mongodb.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import ognl.Ognl;
import ognl.OgnlException;

public class TestOgnlWithMap {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		final Map ctx0 = new HashMap();
		Random random = new Random();
		BSONObject obj = new BasicBSONObject();
		obj.put("count", -1);
		ctx0.put("obj",obj);
		
		BSONObject prev = new BasicBSONObject();
		prev.put("count", 1);
		ctx0.put("prev",prev);
		
		System.out.println(Ognl.getValue("prev.count=prev.count+1,obj.count>0?(obj.count = obj.count+1):(obj.count = obj.count+10)",new HashMap(),ctx0));
		System.out.println(prev.get("count"));
	}

}
