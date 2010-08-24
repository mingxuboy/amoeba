package com.meidusa.amoeba.parser.statement;

import com.meidusa.amoeba.parser.dbobject.Table;


public abstract class AbstractStatement implements Statement {
	protected Table[]                            tables;
	private int parameterCount;
	
	private boolean isPrepared;
	
	public int getParameterCount() {
		return parameterCount;
	}

	public String getSql(){
		return null;
	}
	public void setParameterCount(int count){
		this.parameterCount = count;
	}

	public Table[] getTables() {
        return tables;
    }

    public void setTables(Table[] tables) {
        this.tables = tables;
    }

	public boolean isPrepared() {
		return isPrepared;
	}

	public void setPrepared(boolean isPrepared) {
		this.isPrepared = isPrepared;
	}
}
