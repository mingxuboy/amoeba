package com.meidusa.amoeba.util;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CharsetCache {

	private static Map<String,Charset> charsetMap = new HashMap<String,Charset>();
	public static Charset getCharset(String charsetName){
		Charset charset = charsetMap.get(charsetName);
    	if(charset == null ){
    		synchronized (charsetMap) {
    			charset = charsetMap.get(charsetName);
            	if(charset == null ){
            		charset = Charset.forName(charsetName);
            		charsetMap.put(charsetName, charset);
            	}
			}
    	}
    	
    	return charset;
	}
}
