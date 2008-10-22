package com.meidusa.amoeba.xmltable;

import java.util.HashMap;
import java.util.Map;

public class XmlRow {
	private Map<String,XmlColumn> columMap = new HashMap<String,XmlColumn>();
	
	public Map<String, XmlColumn> getColumMap() {
		return columMap;
	}

	public void setColumMap(Map<String, XmlColumn> columMap) {
		this.columMap = columMap;
	}

	public void addColumn(String name,XmlColumn column){
		columMap.put(name, column);
	}
}
