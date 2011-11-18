package com.meidusa.amoeba.mysql.test.parser;




import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
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
import com.meidusa.amoeba.util.StringUtil;

public class MysqlParserTest {
	
	
	static Map<Column,Comparative> columnMap = new HashMap<Column,Comparative>();
	public static void main(String[] args) throws Exception{
		Map<String,Function> funMap = AbstractQueryRouter.loadFunctionMap("./build/build-mysql/conf/functionMap.xml");
		if(args.length == 0){
		
			List<String> sqlList = XmlToSqlList.executeXml2List(XmlToSqlList.class.getResourceAsStream("sql.xml"));
			for(String sql:sqlList){
				if(sql != null){
					String s = new String(new char[]{(char)0x5c,(char)0x5c});
					sql = StringUtil.replace(sql,s,"");
					s = new String(new char[]{(char)0x5c,(char)0x27});
					sql = StringUtil.replace(sql,s,"");
				}
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
