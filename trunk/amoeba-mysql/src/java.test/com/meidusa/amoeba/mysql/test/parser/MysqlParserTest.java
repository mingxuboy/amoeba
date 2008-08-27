package com.meidusa.amoeba.mysql.test.parser;




import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.statment.DMLStatment;
import com.meidusa.amoeba.parser.statment.PropertyStatment;
import com.meidusa.amoeba.parser.statment.Statment;
import com.meidusa.amoeba.parser.expression.Expression;
import com.meidusa.amoeba.parser.function.*;
import com.meidusa.amoeba.mysql.parser.sql.MysqlParser;
import com.meidusa.amoeba.route.AbstractQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;

public class MysqlParserTest {
	
	
	static Map<Column,Comparative> columnMap = new HashMap<Column,Comparative>();
	public static void main(String[] args){
		Map<String,Function> funMap = AbstractQueryRouter.loadFunctionMap("./conf/functionMap.xml");
		String t = "`asdfasdfaf`";
		System.out.println(t.substring(1,t.length()-1));
		String sql1 = " SELECT * from account where time = DATE_ADD('1998-01-02', INTERVAL 31 DAY)";
		String sql2 = "select * from account where 1<2 and not (id between 12+12 and 33) and id >12+3 or id not in (11,33,'23234') or  id  in (select test.ref_Id from test dd where dd.name='test')";
		String[] sqls = {
				sql1,sql2,
				"SELECT '1997-12-31 23:59:59' + INTERVAL 1 MICROSECOND",
				"SELECT *,asdf from dd where id = hour('11:12:11.123451')",
				"SELECT 2 mod 9",
				"select mod(2,9)",
				"SELECT * from test where gmt_create=YEAR('1998-02-03')",
				"select now()+0",
				"SELECT Current_Date",
				"SELECT * from test where id = week(SYSDATE())",
				"SELECT * from test where name='asdfafd' || 123",
				"SELECT * from test where id = now()+1",
				"select work from account where level =1",
				"Set names utf8",
				"SET  SESSION  TRANSACTION ISOLATION LEVEL read COMMITTED",
				"start transaction",
				"select * from test where id =  ascii('asf')",
				"SELECT * from test where id = INSERT('Quadratic', 3, 4, 'What');",
				"select Instr('ddaass','aas')",
				"insert into  mytable(id,name) values(Instr('ddaass','aas'),INSERT('Quadratic', 3, 4, 'What'))",
				"SET OPTION SQL_SELECT_LIMIT=DEFAULT,@@global.sort_buffer_size=1000000, @@local.sort_buffer_size=1000000",
				"insert into account set id=1002, name='qwerqwer' ,create_time=(33+12)",
				"select `create-time` from account where `game-1`=1",
				"select `create-time` from `account-table` where `game-1`=UNKNOWFUNCTION()",
				"SELECT * FROM `roster-groups` WHERE `collection-owner` = 'wadd@im17.vsa.com.cn' ORDER BY `object-sequence`",
				"SELECT * , member_Blink.qq AS mqq FROM autoSiteShop    LEFT JOIN shopDetail ON autoSiteShop.id = shopDetail.shopId",
				"SELECT adsfad , member_Blink.qq AS mqq   FROM autoSiteShop   LEFT JOIN shopDetail ON autoSiteShop.id = shopDetail.shopId  LEFT JOIN member_Blink ON autoSiteShop.id = member_Blink.memberId     WHERE autowebsite = 'y'     AND id = 6388",
				"insert into test.test1 values('asdfadf',111,11123)",
				"select * from test.test1"
		};
		
		for(String sql: sqls){
			MysqlParser parser = new MysqlParser(new StringReader(sql));
			parser.setFunctionMap(funMap);
			try {
				Statment statment = parser.doParse();
				if(statment instanceof DMLStatment){
					DMLStatment dmlStatment = (DMLStatment)statment;
					Expression expression = dmlStatment.getExpression();
					System.out.println(sql+" =[ "+ expression+"], evaluated = {"+dmlStatment.evaluate(null)+"}");
				}else if(statment instanceof PropertyStatment ){
					PropertyStatment proStatment = (PropertyStatment)statment;
					System.out.println(proStatment.getProperties());
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
