/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mysql.parser;

import java.io.StringReader;
import java.util.regex.Pattern;

import com.meidusa.amoeba.mysql.net.MysqlConnection;
import com.meidusa.amoeba.mysql.parser.sql.MysqlParser;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.expression.Expression;
import com.meidusa.amoeba.parser.statment.PropertyStatment;
import com.meidusa.amoeba.route.AbstractQueryRouter;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class MysqlQueryRouter extends AbstractQueryRouter{
	
	@Override
	public Parser newParser(String sql) {
		return new MysqlParser(new StringReader(sql));
	}

	protected ObjectPool[] selectPool(DatabaseConnection connection,String sql,boolean ispreparedStatment,Object[] parameters){
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
			if(sql.subSequence(0, 4).toString().equalsIgnoreCase("show")){
				return this.defaultPools;
			}
		}
		return super.selectPool(connection, sql, ispreparedStatment, parameters);
	}
	@Override
	protected void setProperty(DatabaseConnection conn, PropertyStatment statment,Object[] parameters) {
		Expression value = null;
		if((value = statment.getValue("autocommit")) != null){
			
			//暂时不支持非自动提交
			/*if(((Long)comparable).longValue() == 1){
				conn.setAutoCommit(true);
			}else{
				conn.setAutoCommit(false);
			}*/
		}else if((value = statment.getValue("names")) != null){
			((MysqlConnection)conn).setCharset((String)value.evaluate(parameters));
		}else if((value = statment.getValue("charset")) != null){
				((MysqlConnection)conn).setCharset((String)value.evaluate(parameters));
		}else if((value = statment.getValue("transactionisolation")) != null){
			//conn.setTransactionIsolation((int)((Long)comparable).longValue());
		}else if((value = statment.getValue("schema")) != null){
			conn.setSchema((String)value.evaluate(parameters)); 
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
