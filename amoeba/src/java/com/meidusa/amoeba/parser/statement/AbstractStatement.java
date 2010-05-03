package com.meidusa.amoeba.parser.statement;


public abstract class AbstractStatement implements Statement {

	private int parameterCount;
	
	public int getParameterCount() {
		return parameterCount;
	}

	public String getSql(){
		return null;
	}
	public void setParameterCount(int count){
		this.parameterCount = count;
	}

}
