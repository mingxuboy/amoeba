package com.meidusa.amoeba.route;

public class SqlQueryObject implements Request{
	public boolean isPrepared;
	public String sql;
	public Object[] parameters;
	public boolean isRead;
	
	@Override
	public boolean isPrepared() {
		return isPrepared;
	}
	
	@Override
	public boolean isRead() {
		return isRead;
	}
}
