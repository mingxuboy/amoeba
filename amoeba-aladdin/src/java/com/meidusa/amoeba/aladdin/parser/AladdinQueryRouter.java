package com.meidusa.amoeba.aladdin.parser;

import java.io.StringReader;

import com.meidusa.amoeba.aladdin.parser.sql.AladdinParser;
import com.meidusa.amoeba.mysql.net.MysqlConnection;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.expression.Expression;
import com.meidusa.amoeba.parser.statement.PropertyStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.SqlBaseQueryRouter;
import com.meidusa.amoeba.route.SqlQueryObject;

/**
 * @author struct
 */
public class AladdinQueryRouter extends SqlBaseQueryRouter {

    @Override
    public Parser newParser(String sql) {
        return new AladdinParser(new StringReader(sql));
    }

	protected void setProperty(DatabaseConnection conn, Statement st,SqlQueryObject parameters) {
		Expression value = null;
		PropertyStatement statment = (PropertyStatement)st;
		if((value = statment.getValue("autocommit")) != null){
			
			//暂时不支持非自动提交
			/*if(((Long)comparable).longValue() == 1){
				conn.setAutoCommit(true);
			}else{
				conn.setAutoCommit(false);
			}*/
		}else if((value = statment.getValue("names")) != null){
			((MysqlConnection)conn).setCharset((String)value.evaluate(parameters.parameters));
		}else if((value = statment.getValue("charset")) != null){
				((MysqlConnection)conn).setCharset((String)value.evaluate(parameters.parameters));
		}else if((value = statment.getValue("transactionisolation")) != null){
			//conn.setTransactionIsolation((int)((Long)comparable).longValue());
		}else if((value = statment.getValue("schema")) != null){
			conn.setSchema((String)value.evaluate(parameters.parameters)); 
		}
	}

}
