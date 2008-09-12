package com.meidusa.amoeba.oracle.parser;

import java.io.StringReader;

import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.oracle.parser.sql.OracleParser;
import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.statment.PropertyStatment;
import com.meidusa.amoeba.route.AbstractQueryRouter;

/**
 * 
 * @author struct
 *
 */
public class OracleQueryRouter extends AbstractQueryRouter {

	@Override
	public Parser newParser(String sql) {
		return new OracleParser(new StringReader(sql));
	}

	@Override
	protected void setProperty(DatabaseConnection conn,
			PropertyStatment statment, Object[] parameters) {
	}

}
