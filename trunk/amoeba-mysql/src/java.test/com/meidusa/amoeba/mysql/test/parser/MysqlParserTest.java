package com.meidusa.amoeba.mysql.test.parser;




import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.statement.DMLStatement;
import com.meidusa.amoeba.parser.statement.PropertyStatement;
import com.meidusa.amoeba.parser.statement.ShowStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.parser.expression.Expression;
import com.meidusa.amoeba.parser.function.*;
import com.meidusa.amoeba.mysql.parser.sql.MysqlParser;
import com.meidusa.amoeba.route.AbstractQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;

public class MysqlParserTest {
	
	
	static Map<Column,Comparative> columnMap = new HashMap<Column,Comparative>();
	public static void main(String[] args) throws Exception{
		Map<String,Function> funMap = AbstractQueryRouter.loadFunctionMap("./build/build-mysql/conf/functionMap.xml");
		parser(funMap,"delete from aa where id=11 limit 100,?");
		parser(funMap,"select ID, SDID, APP_ID, PHOTO_ID, ALBUM_ID, IMAGE_NAME, DESCRIPTION, BROWSE_COUNT, SHARE_COUNT,       REVIEW_COUNT, IMPRESSION_COUNT, SHAREABLE, REVIEWABLE, AUDIT_RESULT, HAS_EXIF, CAPACITY, SIZE,       UPLOAD_IP, LOCATION, CREATE_TIME, LAST_MODIFY_TIME, TAG1, IMAGE_PATH_VERSION, IMAGE_BASE_URL,       FORMAL ,FILE_TYPE ,PRIVILEGE,\"\" as EXIF,DEL_FLAG     from SD_PHOTO.IMAGE     where SDID=1250873924 AND DEL_FLAG = 0                      AND          PRIVILEGE & 1 =1                    AND APP_ID IN (    '8'      )                   ORDER BY  CREATE_TIME DESC                   LIMIT 0,30");
		parser(funMap,"select i.id,i.title,i.img_url,i.link,i.`order`,i.type_id from imgdemo AS i where display_type=1 and type_id=9 order by i.`order`");
		parser(funMap,"SELECT `n`.*, `au`.`user_true_name`, `ct`.`type_name` FROM `content` AS `n` JOIN `admin_user` AS `au` ON (`n`.`author` = `au`.`user_id`) JOIN `content_type` AS `ct` ON (`n`.`content_type` = `ct`.`type_id`) WHERE `content_type` = 15 AND `n`.`status` = 1 ORDER BY `n`.`content_type` ASC, `n`.`is_first` DESC, `n`.`content_id` DESC LIMIT 0, 6");
		parser(funMap,"SELECT `c`.* FROM `city` AS `c` LEFT JOIN `shops` AS `s` ON (`s`.`city_id` = `c`.`city_id`) WHERE `s`.`shop_status` = 1 AND `s`.`booking_start_date` <= '2010-08-25 11:37:22' and `c`.city='weqwerq'	GROUP BY `c`.`city_id`");
		parser(funMap,"select lease_price,global_price,advance_price,advance_price2,outlicheng_money,timeout_price,delay_work,pay_points from cartype_price where status=1 and '2010-08-25 09:20:56' BETWEEN start_datetime and end_datetime AND cartype_id=27 AND city_id=21 AND price_type=1 order by end_datetime desc limit 1");
		parser(funMap,"SELECT * FROM d_visitor.t_visitor_visit WHERE `user`='1235625354' and visitors<>'1235625354' ORDER BY date DESC LIMIT 0,30");
		parser(funMap,"SELECT PRIVILEGE,PRIVILEGE & 1   FROM SD_PHOTO.IMAGE    WHERE SDID=13916728412 and PRIVILEGE = 1& DD" );
		
		parser(funMap,"select count(tfi.Financing_Invest_ID) as investCount  from tbl_financing_invest as tfi,tbl_financing_event as tfe where tfe.HappenDate between DATE_FORMAT(DATE_ADD(Now(), Interval -10 year),'%Y') and DATE_FORMAT(NOW(),'%Y') and tfi.FinancingEvent_ID = tfe.Financing_Event_ID and tfi.Organization_ID =3;");
		parser(funMap,"SELECT SOURCE_UUID  FROM SD_FEED.FEED  FORCE INDEX(INDX_SDID_DFALG_ATYPE_CRTTIME)" 
				+" WHERE DEL_FLAG = 0 AND   SDID IN   (    1247553060   ,    1247832413   ,    1181647770   ,    1248002785   ,    1248203227   ,    1247999383   ,    1247811558   ,    1181607039   ,    1221592290   ,    1246478650   ,    1109266877   ,    999259458   ,    595684235   ,    658423815   ,    1109949156   ,    1145903891   ,    1034904986   ,    1180096992   ,    216712   ,    937839624   )       AND     ACTIVITY_TYPE IN     (      1002003       ,      1002005       ,      1002007       ,      1002009       ,      1002011       )"            
				+" AND  ACTIVITY_TYPE IN ( 1002003       ,      1002005)  AND  CREATE_TIME                  >          1277572966915"      
				+" AND     SOURCE_SDID = SDID ORDER BY CREATE_TIME       ASC          LIMIT 30;");
		parser(funMap,"SELECT UUID, CREATE_TIME, CONTENT, ACTIVITY_TYPE, COMPRESS_FLAG   FROM SD_FEED.FEED_CONTENT   WHERE `UUID`='3ef88ee7e8994d1796291f64c83d2edd' ");
		parser(funMap,"insert into TEST_SD_RELATION.RELATION_REVERSE ( SDID, F_SDID, STATE,   CREATE_TIME,   LAST_MODIFY_TIME, DEL_FLAG)   values (772152974,   1115596664,   1,   UNIX_TIMESTAMP()*1000,   UNIX_TIMESTAMP()*1000,0)  ON DUPLICATE KEY   UPDATE DEL_FLAG=0,   LAST_MODIFY_TIME=UNIX_TIMESTAMP()*1000;");
		
		parser(funMap,"EXPLAIN SELECT REPLACE(UUID(), '-', '')");
		parser(funMap," select * from moodrecord.t_twitter_hot where `key`='asdfasfd' order by add_time desc limit ?,?");
		String t = "select ID from SD_MESSAGE.MESSAGE_NOTIFICATION  where  SDID=11 and name=? order by CREATE_TIME limit ?,?";
		String sql1 = " SELECT * from account where time = DATE_ADD('1998-01-02', INTERVAL 31 DAY)";
		String sql2 = "select * from account where 1<2 and not (id between 12+12 and 33) and id >12+3 or id not in (11,33,'23234') or  id  in (select test.ref_Id from test dd where dd.name='test')";
		String[] sqls = {"SELECT COUNT(*) FROM (   SELECT F_SDID   FROM SD_RELATION.RELATION_FOLLOW   WHERE SDID=12 AND DEL_FLAG = 0   GROUP BY (F_SDID)) A",
				sql1,sql2,
				 "UPDATE `uc_app_game`.`sdo_admin_user` SET `usr_sumlogin`=(`usr_sumlogin`+1),`usr_loginip`=1034744162 WHERE `usr_id`=? LIMIT 1,?",
				 "insert into SD_FEED.FEED_CONTENT (UUID, CONTENT, COMPRESS_FLAG, ACTIVITY_TYPE, CREATE_TIME)   values('c5381e2e2aab47828683fee7034c125a', x'7B22616374697669747954797065223A313030323031312C226F626A6563744964223A226331656638306265376463393131646662666231303032363535643764656230222C226D6564616C4964223A393830312C226D6564616C49636F6E223A22687474703A2F2F697069632E73746174696373646F2E636F6D2F76312F696D616765732F6D6564616C2F6D325F6C696D69745F312E706E67222C226D6564616C4E616D65223A22E7B396E69E9CE9A696E58F91E794A8E688B7222C226964223A226335333831653265326161623437383238363833666565373033346331323561222C2266726F6D417070223A332C2263726561746554696D65223A313237373138393038353133342C2273646964223A313235353436363037382C227368617265223A66616C73652C2272657669657761626C65223A66616C73652C2275746354696D65223A22547565204A756E2032322031343A34343A3435204353542032303130227D', 0, 1002011, 1277189085134)",
				"select ID from SD_MESSAGE.MESSAGE_NOTIFICATION  where  SDID=11 order by CREATE_TIME limit ?,?",
				"SELECT /*  #pool */ * FROM AA WHERE ID = 'ASDF\\'ADF'",
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
				"select @@sql_mode",
				"select * from aaa where id = 12 AND (upper(subject) like upper(?) OR upper(keywords) like upper(?))"
				,"REPLACE INTO cdb_spacecaches (uid, variable, value, expiration) VALUES ('2980526', 'mythreads', 'a:3:{i:0;a:14:{s:3:\"tid\";s:7:\"1606800\";s:7:\"subject\";s:8:\"赤版請進\";s:7:\"special\";s:1:\"0\";s:5:\"price\";s:1:\"0\";s:3:\"fid\";s:3:\"129\";s:5:\"views\";s:2:\"20\";s:7:\"replies\";s:1:\"0\";s:6:\"author\";s:4:\"av8d\";s:8:\"authorid\";s:7:\"2980526\";s:8:\"lastpost\";s:10:\"1236516949\";s:10:\"lastposter\";s:4:\"av8d\";s:10:\"attachment\";s:1:\"0\";s:3:\"pid\";s:6:\"154418\";s:7:\"message\";s:191:\"刪我的帖, 我無所謂, 但是總不能把損我的帖子留在那吧?http://bbs.macd.cn/viewthread.php?tid=1585698&amp;extra=page%3D1&amp;authorid=0&amp;page=33656F, 657樓, 660F, 都Quote了損我的言論. 不該刪嗎?\";}i:1;a:14:{s:3:\"tid\";s:7:\"1594957\";s:7:\"subject\";s:25:\"Q&amp;A With Bob Prechter\";s:7:\"special\";s:1:\"0\";s:5:\"price\";s:1:\"0\";s:3:\"fid\";s:2:\"22\";s:5:\"views\";s:3:\"428\";s:7:\"replies\";s:2:\"10\";s:6:\"author\";s:4:\"av8d\";s:8:\"authorid\";s:7:\"2980526\";s:8:\"lastpost\";s:10:\"1234267824\";s:10:\"lastposter\";s:9:\"cixilarty\";s:10:\"attachment\";s:1:\"0\";s:3:\"pid\";s:4:\"8429\";s:7:\"message\";s:289:\"我不翻譯了, 繁體的翻譯可能大家不習慣,還是看原文的吧!沒多少單字的.:*29*:Q&amp;A With Bob PrechterThe following is a compilation of Bob Prechter\\'s best media interviews.In this Q&amp;A, Bob talks about the validity and practical applicationsof the Wave Principle and explains Socionomics,...\";}i:2;a:14:{s:3:\"tid\";s:7:\"1593908\";s:7:\"subject\";s:27:\"已超过规定多次 按版规该封IP\";s:7:\"special\";s:1:\"0\";s:5:\"price\";s:1:\"0\";s:3:\"fid\";s:3:\"129\";s:5:\"views\";s:2:\"44\";s:7:\"replies\";s:1:\"2\";s:6:\"author\";s:4:\"av8d\";s:8:\"authorid\";s:7:\"2980526\";s:8:\"lastpost\";s:10:\"1234077272\";s:10:\"lastposter\";s:5:\"嘘。0\";s:10:\"attachment\";s:1:\"0\";s:3:\"pid\";s:3:\"910\";s:7:\"message\";s:282:\"1. 依据 http://bbs.macd.cn/thread-615952-1-1.html 5楼 重阳版主规定, 应予以封IP段方式处理.要营造专业、和谐的交流环境，必须加强版面管理。在原有的版规下增加一些细节：（于2009年1月10日执行）5.使用恶毒语言诅咒他人的，即时禁止登陆（记录在案，禁止其马甲进入论坛）。6.使用马甲进行捣乱的 ...\";}}', '1238258538')",
				"select * from users where user like '%rain%';",
				"SELECT magid, title FROM mag WHERE parentid = '72' ORDER BY rand() LIMIT 10",
				"EXPLAIN  select distinct(a.id),a.InfoTitle,b.corpName,a.ProPrice,a.ShowTime,a.ExpTime,d.user as user_name,b.province,b.city,a.ProIntro from blogs c, users d,provide_info a ,corp_info b ,keyword e where a.blog_id=b.blog_id and a.blog_id!= '85653' and now() - INTERVAL a.InfoExp DAY  and a.blog_id=c.id and c.owner_id=d.id and a.id=e.host_id and e.ktype=4 and e.kname='气动行业' order by a.ShowTime desc,a.id desc",
				"EXPLAIN SELECT * FROM xx where id= 12 FORCE INDEX (xx,yyy)",
				"INSERT INTO cdb_threadsmod (tid, uid, username, dateline, action, expiration, status) VALUES ('1679374', '193262', '椋炶垷', '1248835442', 'MOV', '0', '1'), ('1679382', '193262', '椋炶垷', '1248835442', 'MOV', '0', '1'); ",
				"SELECT groupid, type, grouptitle FROM cdb_usergroups ORDER BY (creditshigher<>'0' || creditslower<>'0'), creditslower;",
				"SELECT uid, COUNT(*) AS count FROM cdb_sessions GROUP BY uid='0';",
				"(select help_topic_id ,name from mysql.help_topic where help_topic_id=53 order by help_category_id desc limit 2) union all (select help_topic_id ,name from mysql.help_topic where help_topic_id=47 order by help_category_id desc limit 2) union all (select help_topic_id ,name from mysql.help_topic where help_topic_id=53 order by help_category_id desc limit 2) union all (select help_topic_id ,name from mysql.help_topic where help_topic_id=47 order by help_category_id desc limit 2)",
				"select version() * 100;",
				"SELECT * FROM bbs_doing USE INDEX(dateline) WHERE 1 ORDER BY dateline DESC LIMIT 0,20;",
				"SELECT * FROM TABLE1 force INDEX (ss)",
				"SELECT FOUND_ROWS()",
				"insert into SD_MESSAGE.TOPIC_CONTENT(TOPIC_ID , CREATE_TIME , TOPIC_CONTENT) values ('c10795a20c3242299e669c29b4486958' ,UNIX_TIMESTAMP()*1000 ,'testInertTopic')",
				"SELECT `user_base`.`user_id`, `user_base`.`email`, `user_base`.`fullname`, `user_base`.`gender`, `user_base`.`status_content`, `user_base`.`status_update_time`, `user_base`.`tower_id`, `user_base`.`city_domain` FROM `user_base` where `user_base` = 1000 ORDER BY `user_base`.`user_id` DESC LIMIT 100 OFFSET 100"
				,"/* mysql-connector-java-5.1.6 ( Revision: ${svn.Revision} ) */ SHOW VARIABLES WHERE Variable_name ='language' OR Variable_name = 'net_write_timeout' OR Variable_name = 'interactive_timeout' OR Variable_name = 'wait_timeout' OR Variable_name = 'character_set_client' ",
				"  SELECT RF.F_SDID FROM SD_RELATION.RELATION_FOLLOW AS RF LEFT JOIN SD_RELATION.RELATION_REVERSE AS RR USING (SDID,F_SDID) WHERE RF.SDID=322 AND RF.DEL_FLAG = 0 AND (RR.DEL_FLAG = 1 OR RR.F_SDID IS NULL) GROUP BY (F_SDID);",
				"INSERT INTO test (a,b) VALUES ('1','2') ON  DUPLICATE  KEY  UPDATE ID=LAST_INSERT_ID(ID),Dummy = 4",
				"UPDATE `agency`.`property` SET `PropertyID`='2010041131894474                ',`CityName`='南昌',`DistrictName`='东湖',`EstateID`='0706231710370C36FB840B3AFFA0E46D',`RoomNo`='88',`PropertyDirection`='东西',`MgtPrice`=0.0000,`DeptID`='0909291943174B44614C252A03703E88',`EmpID`='04616019F75B4F4B92029C4D5C2B8542',`PropertyFloor`='',`ModPerson`='汲燕丽',`ModDate`=_latin1'2010-04-11 00:21:56',`Privy`=0 WHERE `PropertyID`='2010041131894474' AND `CityName`='南昌' AND `DistrictName`='东湖' AND `EstateID`='0706231710370C36FB840B3AFFA0E46D' AND `RoomNo`='44' AND `PropertyDirection`='东西' AND `MgtPrice`=0.0000 AND `DeptID`='0909291943174B44614C252A03703E88' AND `EmpID`='04616019F75B4F4B92029C4D5C2B8542' AND `PropertyFloor`='' AND `ModPerson`='汲燕丽' AND `ModDate`=_latin1'2010-04-11 00:14:23' AND `Privy`=0",
				"insert into UC_PROFILE.USER_PROFILE_BASIC (SDID ,AVATAR_VERSION,REGISTER_IP,NICK_LAST_MODIFY_TIME,LEVEL,ACTIVE_FLAG,DEL_FLAG,CREATE_TIME,LAST_MODIFY_TIME,REGISTER_TIME)values (? , ?  ,INET_ATON(?),UNIX_TIMESTAMP()*1000,0,1,0,UNIX_TIMESTAMP()*1000,UNIX_TIMESTAMP()*1000,UNIX_TIMESTAMP()*1000)"
		};
		if(args.length == 0){
			for(String sql: sqls){
				parser(funMap,sql);
			}
		}else{
			BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
			StringBuffer buffer = new StringBuffer();
			String line = null;
			while((line = reader.readLine()) != null){
				if(line.trim().startsWith("#")){
					continue;
				}else{
					buffer.append(line).append("\n");
				}
			}
			String sql = buffer.toString();
			parser(funMap,sql);
			
			
		}
	}
	
	private static void parser(Map<String,Function> funMap,String sql){
		Parser parser = new MysqlParser(new StringReader(sql));
		parser.setFunctionMap(funMap);
		try {
			Statement statment = parser.doParse();
			if(statment instanceof DMLStatement){
				DMLStatement dmlStatment = (DMLStatement)statment;
				Expression expression = dmlStatment.getExpression();
				System.out.println(sql+" =[ "+ expression+"], evaluated = {"+dmlStatment.evaluate(null)+"} ,parameterCount="+dmlStatment.getParameterCount());
			}else if(statment instanceof PropertyStatement ){
				PropertyStatement proStatment = (PropertyStatement)statment;
				System.out.println(proStatment.getProperties());
			}else if(statment instanceof ShowStatement){
				ShowStatement proStatment = (ShowStatement)statment;
				System.out.println(proStatment.getExpression());
			}
			
		} catch (Exception e) {
			System.out.println("---------------------------------");
			System.out.println("error sql:"+ sql);
			e.printStackTrace();
			System.out.println("--------------------------");
		}
	}
	
}
