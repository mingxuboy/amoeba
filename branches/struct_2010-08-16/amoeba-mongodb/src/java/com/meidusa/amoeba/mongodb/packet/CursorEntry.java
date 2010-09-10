package com.meidusa.amoeba.mongodb.packet;

import org.apache.commons.lang.StringUtils;

public class CursorEntry {
	public long cursorID;
	public String fullCollectionName;
	
	public int hashcode(){
		return fullCollectionName.hashCode() + (int)cursorID;
	}
	
	public boolean equals(Object object){
		if(object instanceof CursorEntry){
			CursorEntry entry = (CursorEntry)object;
			return (cursorID == entry.cursorID) && (StringUtils.equals(fullCollectionName, entry.fullCollectionName));
		}
		return false;
	}
}
