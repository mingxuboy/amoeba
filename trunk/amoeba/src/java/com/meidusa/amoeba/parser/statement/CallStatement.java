package com.meidusa.amoeba.parser.statement;

public class CallStatement extends DMLStatement {
	public CallStatement(){
		this.setProcedure(true);
	}
}
