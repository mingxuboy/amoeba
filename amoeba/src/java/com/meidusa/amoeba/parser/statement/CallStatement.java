package com.meidusa.amoeba.parser.statement;

public class CallStatement extends DMLStatement {
	private boolean isRead;
	@Override
	public boolean isReadStatement() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	
}
