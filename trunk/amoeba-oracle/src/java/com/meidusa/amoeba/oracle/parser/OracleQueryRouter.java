package com.meidusa.amoeba.oracle.parser;

import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.statment.PropertyStatment;
import com.meidusa.amoeba.route.AbstractQueryRouter;

public class OracleQueryRouter extends AbstractQueryRouter {

	@Override
	public Parser newParser(String sql) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setProperty(DatabaseConnection conn,
			PropertyStatment statment, Object[] parameters) {
		// TODO Auto-generated method stub

	}

}
