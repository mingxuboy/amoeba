package com.meidusa.amoeba.mongodb.route;

import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.parser.expression.Expression;
import com.meidusa.amoeba.parser.statement.Statement;


public class MongodbStatement implements Statement {
	protected Table table;
	
	public int getParameterCount() {
		return 0;
	}

	public String getSql(){
		return null;
	}
	public void setParameterCount(int count){
	}

	public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

	@Override
	public Expression getExpression() {
		return null;
	}

	@Override
	public boolean isPrepared() {
		return false;
	}
}
