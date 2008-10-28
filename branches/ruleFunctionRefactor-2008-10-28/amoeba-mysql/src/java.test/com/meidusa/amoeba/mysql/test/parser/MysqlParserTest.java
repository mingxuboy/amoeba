package com.meidusa.amoeba.mysql.test.parser;




import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.parser.Parser;
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
				"Set names utf8","set names latin1",
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
				"select * from test.test1",
				"REPLACE INTO supe_spacecache9(uid, cacheid, value, updatetime) VALUES ('81828', '9', 'a:0:{}', '1219970056')",
				"REPLACE INTO supe_members (uid, groupid, username, password, secques, timeoffset, dateformat, havespace, newpm) VALUES ('219733', '9', 'KennisWai', '825e73d2764708bc30d4f401c4720f3a', '', '9999', '', '0', '0')",
				"SELECT sid, uid AS sessionuid, groupid, groupid='6' AS ipbanned, pageviews AS spageviews, styleid, lastolupdate, seccode FROM cdb_sessions WHERE sid='CgShIZ' AND CONCAT_WS('.',ip1,ip2,ip3,ip4)='210.177.156.49'",
				"SELECT t.tid, t.closed, t.dateline, t.special, t.lastpost AS lastthreadpost,  f.*, ff.*  , f.fid AS fid "+
					"FROM cdb_threads t INNER JOIN cdb_forums f ON f.fid=t.fid	LEFT JOIN cdb_forumfields ff ON ff.fid=f.fid  WHERE t.tid='1397087' AND t.displayorder>='0' LIMIT 1",
				"SELECT f.fid, f.fup, f.type, f.name, f.threads, f.posts, f.todayposts, f.lastpost, f.inheritedmod, f.forumcolumns, f.simple, ff.description, ff.moderators, ff.icon, ff.viewperm, ff.redirect FROM cdb_forums f LEFT JOIN cdb_forumfields ff USING(fid)	WHERE f.status>0 ORDER BY f.type, f.displayorder",
				"SELECT o.* FROM  (SELECT row_id   FROM  (SELECT row_id,    rownum rn     FROM    (SELECT rowid row_id       FROM offer      WHERE member_id = ?    AND status        = ?    AND gmt_expire    > sysdate    AND type = ?   ORDER BY MEMBER_ID,      STATUS         ,      GMT_EXPIRE DESC    )    WHERE rownum<=?  )  WHERE rn >= ?  ) t,  offer o  WHERE t.row_id=o.rowid ",
				"set CLIENT CHARSET gbk",
				"select * from offer where id in(12,11) limit 1,2",
				"SELECT d_tax, d_next_o_id FROM district WHERE d_w_id = 1  AND d_id = 1 FOR UPDATE",
				"select @@sql_mode"
		};
		
		for(String sql: sqls){
			Parser parser = new MysqlParser(new StringReader(sql));
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
