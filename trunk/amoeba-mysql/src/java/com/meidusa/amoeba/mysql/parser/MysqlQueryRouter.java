/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mysql.parser;

import java.io.StringReader;

import com.meidusa.amoeba.mysql.net.MysqlConnection;
import com.meidusa.amoeba.mysql.parser.sql.MysqlParser;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.expression.Expression;
import com.meidusa.amoeba.parser.statement.PropertyStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.SqlBaseQueryRouter;
import com.meidusa.amoeba.route.SqlQueryObject;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class MysqlQueryRouter extends SqlBaseQueryRouter{
	
	@Override
	public Parser newParser(String sql) {
		return new MysqlParser(new StringReader(sql));
	}

	public ObjectPool[] selectPool(DatabaseConnection connection,SqlQueryObject queryObject){
		String sql = queryObject.sql;
		if(sql != null){
			sql = sql.trim();
			while(sql.startsWith("/*")){
				int index = sql.indexOf("*/");
				if(index >0){
					sql = sql.substring(index+2);
				}else{
					break;
				}
				sql = sql.trim();
			}
			if(sql.length()>4){
				String sqlHead = sql.substring(0, 4);
				if(sqlHead.equalsIgnoreCase("show") || sqlHead.equalsIgnoreCase("help") ){
					return defaultPools;
				}
			}
		}
		return super.selectPool(connection, queryObject);
	}
	@Override
	protected void setProperty(DatabaseConnection conn, Statement st,SqlQueryObject queryObject) {
		PropertyStatement statment = (PropertyStatement)st;
		Expression value = null;
		if((value = statment.getValue("autocommit")) != null){
			
			//暂时不支持非自动提交
			/*if(((Long)comparable).longValue() == 1){
				conn.setAutoCommit(true);
			}else{
				conn.setAutoCommit(false);
			}*/
		}else if((value = statment.getValue("names")) != null){
			((MysqlConnection)conn).setCharset((String)value.evaluate(queryObject.parameters));
		}else if((value = statment.getValue("charset")) != null){
			((MysqlConnection)conn).setCharset((String)value.evaluate(queryObject.parameters));
		}else if((value = statment.getValue("character_set_results")) != null){
			((MysqlConnection)conn).setCharset((String)value.evaluate(queryObject.parameters));
		}else if((value = statment.getValue("transactionisolation")) != null){
			//conn.setTransactionIsolation((int)((Long)comparable).longValue());
		}else if((value = statment.getValue("schema")) != null){
			conn.setSchema((String)value.evaluate(queryObject.parameters)); 
		}
	}
	
	public static void main(String[] args){
		String sql = " /* sdfqwer */ /* asdfqer */ show asdf";
		sql = sql.trim();
		while(sql.startsWith("/*")){
			int index = sql.indexOf("*/");
			if(sql.startsWith("/*") && index >0){
				sql = sql.substring(index+2);
			}
			sql = sql.trim();
		}
		System.out.println(sql);
	}
}
