package com.meidusa.amoeba.parser.statement;

import com.meidusa.amoeba.parser.dbobject.Table;


public abstract class AbstractStatement implements Statement {
	protected Table[]                            tables;
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

	public Table[] getTables() {
        return tables;
    }

    public void setTables(Table[] tables) {
        this.tables = tables;
    }
}
