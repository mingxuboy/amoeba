package com.meidusa.amoeba.aladdin.handler;

import java.sql.Types;

import com.meidusa.amoeba.jdbc.ResultSetHandler;
import com.meidusa.amoeba.net.DatabaseConnection;

public class StringTypeHandler implements ResultSetHandler {
	private String amoebaCharset;
	
	public String getAmoebaCharset() {
		return amoebaCharset;
	}

	public void setAmoebaCharset(String amoebaCharset) {
		this.amoebaCharset = amoebaCharset;
	}

	public <T> T clientToServer(DatabaseConnection conn, T object) {
		return null;
	}

	public <T> T serverToClient(DatabaseConnection conn, T object) {
		return null;
	}

	public boolean needHandle(int jdbcType) {
		switch(jdbcType){
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
				return true;
			default: return false;
		}
	}

}
