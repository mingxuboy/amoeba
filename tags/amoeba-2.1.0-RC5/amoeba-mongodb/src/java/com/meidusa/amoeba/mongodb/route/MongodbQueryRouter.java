/*
 * Copyright amoeba.meidusa.com
 * 
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
package com.meidusa.amoeba.mongodb.route;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.bson.BSONObject;
import org.bson.JSON;
import org.bson.types.BSONTimestamp;
import org.bson.types.BasicBSONList;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.dbobject.Schema;
import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.route.AbstractQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;
import com.meidusa.amoeba.sqljep.function.ComparativeAND;
import com.meidusa.amoeba.sqljep.function.ComparativeBaseList;
import com.meidusa.amoeba.sqljep.function.ComparativeOR;
import com.meidusa.amoeba.util.StringUtil;
import com.meidusa.amoeba.util.ThreadLocalMap;

/**
 * 
 * @author Struct
 *
 */
@SuppressWarnings("unchecked")
public class MongodbQueryRouter extends AbstractQueryRouter<MongodbClientConnection,RequestMongodbPacket> {
	private static Map<String,Integer> operatorMap = new HashMap<String,Integer>();
	private static ThreadLocal<Stack<Comparative>> threadLocal = new ThreadLocal<Stack<Comparative>>(){
		protected Stack<Comparative> initialValue() {
	        return new Stack<Comparative>();
	    }
	};
	
	static{
		operatorMap.put("$gt", Comparative.GreaterThan);
		operatorMap.put("$gte", Comparative.GreaterThanOrEqual);
		operatorMap.put("$lt", Comparative.LessThan);
		operatorMap.put("$lte", Comparative.LessThanOrEqual);
		operatorMap.put("$ne", Comparative.NotEquivalent);
		operatorMap.put("$in", Comparative.Equivalent);
		operatorMap.put("$nin", Comparative.NotEquivalent);
		operatorMap.put("$or", Comparative.Equivalent);
		
	}
    
	protected void beforeSelectPool(MongodbClientConnection connection, RequestMongodbPacket queryObject){
		super.beforeSelectPool(connection, queryObject);
		ThreadLocalMap.put(_CURRENT_QUERY_OBJECT_, queryObject);
    }
    
	private void fillTableAndSchema(String fullCollectionName,Table table,Schema schema){
		int	index = fullCollectionName.indexOf(".");
		if(index >0){
			String schemaName = fullCollectionName.substring(0,index);
			String tableName =  fullCollectionName.substring(index +1);
			table.setName(tableName);
			schema.setName(schemaName);
			table.setSchema(schema);
		}else{
			table.setName(fullCollectionName);
		}
	}
	
	@Override
	protected Map<Table, Map<Column, Comparative>> evaluateTable(MongodbClientConnection connection,RequestMongodbPacket queryObject) {
		Table table = new Table();
		Schema schema = new Schema();
		int cmd = 0;
		if(queryObject.fullCollectionName != null){
			
			cmd = queryObject.fullCollectionName.indexOf(".$cmd");
			if(cmd >0){
				String schemaName = queryObject.fullCollectionName.substring(0,cmd);
				schema.setName(schemaName);
				table.setSchema(schema);
			}else{
				fillTableAndSchema(queryObject.fullCollectionName,table,schema);
			}
		}
		BSONObject bson = null;
		
		if(queryObject instanceof QueryMongodbPacket){
			QueryMongodbPacket query = (QueryMongodbPacket)queryObject;
			
			if(cmd >0){
				String tableName = null;
				
				_tableName:{
					tableName = (String)query.query.get("count");
					if(tableName != null){
						bson = (BSONObject)query.query.get("query");
						break _tableName;
					}
					
					tableName = (String)query.query.get("mapreduce");
					if(tableName != null){
						bson = (BSONObject)query.query.get("query");
						break _tableName;
					}
					
					tableName = (String)query.query.get("distinct");
					if(tableName != null){
						bson = (BSONObject)query.query.get("query");
						break _tableName;
					}
					
					BSONObject groupBSObject = (BSONObject)query.query.get("group");
					
					if(groupBSObject != null){
						tableName = (String)groupBSObject.get("ns");
						if(tableName != null){
							bson = (BSONObject)groupBSObject.get("cond");
							break _tableName;
						}
					
					}
					
					tableName = (String)query.query.get("drop");
					if(tableName != null){
						break _tableName;
					}
					
					tableName = (String)query.query.get("deleteIndexes");
					if(tableName != null){
						break _tableName;
					}
					
					if(query.query.get("listDatabases")!= null){
						tableName = "listDatabases";
						schema.setName("admin");
						break _tableName;
					}
					
					if(query.query.get("findandmodify")!= null){
						tableName = (String)query.query.get("findandmodify");
						bson = (BSONObject)query.query.get("query");
						break _tableName;
					}
					
					//db.getSisterDB(\"schema\").getCollection(\"collectionName\").stats();
					if(query.query.get("$eval") != null){
						Object object = query.query.get("$eval");
						if(object instanceof BSONObject){
							BSONObject eval = (BSONObject) object;
							for(String name : eval.keySet()){
								if(name.startsWith("db.getSisterDB")){
									String[] tmps = StringUtil.split(name, "\"'");
									if(tmps.length>3){
										tableName = tmps[3];
										schema.setName(tmps[1]);
									}
									break _tableName;
								}
							}
						}
					}
				}
				
				if(tableName != null){
					table.setName(tableName);
				}else{
					bson = (BSONObject)query.query.get("query");
				}
			}else{
				/*if("system.namespaces".equalsIgnoreCase(table.getName())){
					//schema.setName("*");
				}*/
				bson = query.query;
			}
		}else if(queryObject  instanceof InsertMongodbPacket){
			InsertMongodbPacket query = (InsertMongodbPacket)queryObject;
			if(query.fullCollectionName.endsWith("system.indexes")){
				String tableName = (String)query.documents[0].get("ns");
				fillTableAndSchema(tableName,table,schema);
			}else{
				if(query.documents != null && query.documents.length>0){
					bson = query.documents[0];
				}
			}
		}else if(queryObject  instanceof DeleteMongodbPacket){
			DeleteMongodbPacket query = (DeleteMongodbPacket)queryObject;
			bson =  query.selector;
		}else if(queryObject instanceof UpdateMongodbPacket){
			UpdateMongodbPacket query =(UpdateMongodbPacket)queryObject;
			bson = query.selector;
		}
		
		Map<Table, Map<Column, Comparative>> tableMap = new HashMap<Table, Map<Column, Comparative>>();
		if(bson != null){
			Map<Column, Comparative> parameterMap = new HashMap<Column, Comparative>();
			tableMap.put(table, parameterMap);
			Stack<Comparative> stack = threadLocal.get();
			stack.clear();
			toComparative(parameterMap,stack,bson,table);
			return tableMap;
		}else{
			tableMap.put(table, null);
		}
		return tableMap;
	}
	
	private static void putToColumnMap(Map<Column, Comparative> parameterMap,Column column,Comparative comparative){
		Comparative comp = parameterMap.get(column);
		if(comp == null){
			parameterMap.put(column, comparative);
		}else{
			ComparativeBaseList comparativeList;
			if(comp instanceof ComparativeBaseList){
				comparativeList = (ComparativeBaseList)comp;
			}else{
				comparativeList = new ComparativeOR();
				comparativeList.addComparative(comp);
			}
			comparativeList.addComparative(comparative);
			parameterMap.put(column, comparativeList);
		}
	}
	
	public static void toComparative(Map<Column, Comparative> parameterMap,Stack<Comparative> stack,BSONObject bson,Table table){
		if(bson != null){
			if(bson instanceof BasicBSONList){
				ComparativeBaseList comparativeList = null;
				comparativeList = new ComparativeAND();
				int start = stack.size();
				for(Object object : (BasicBSONList)bson){
					if(object instanceof BSONObject ){
						toComparative(parameterMap,stack,(BSONObject)object,table);
					}else{
						comparativeList.addComparative(new Comparative(Comparative.Equivalent,(Comparable)object));
					}
				}
				
				int end = stack.size();
				if(end - start > 1){
					for(int i = end-start;i>0;i--){
						comparativeList.addComparative((Comparative)stack.pop());
					}
					stack.push(comparativeList);
				}
				return;
			}
			
			Map map = bson.toMap();
			if(map != null && map.size() >0){
				int current = stack.size();
				
				for(Object item : map.entrySet()){
					Map.Entry entry = (Map.Entry)item;
					String name = (String)entry.getKey();
					Object value =  entry.getValue();
					
					Column column = null;
					Comparative comparable = null;
					Integer comparativeValue = null;
					boolean and = false;
					boolean isMulti = false;
					//name
					if(!name.startsWith("$")){
						column = new Column();
						column.setName(name);
						column.setTable(table);
						comparativeValue = Comparative.Equivalent;
					}else{
						
						//if Conditional Operators not in map ,we will ignore the entry,such as '$size'
						comparativeValue = operatorMap.get(name);
						if("$nin".equalsIgnoreCase(name)){
							and = true;
							isMulti = true;
						}else if("$in".equalsIgnoreCase(name)){
							and = false;
							isMulti = true;
						}else if("$or".equalsIgnoreCase(name)){
							and = false;
							isMulti = true;
						}else if("$where".equalsIgnoreCase(name)){
							and = true;
							isMulti = true;
						}
						
						if(comparativeValue == null){
							return;
						}
					}
					
					//value class is BSONObject
					if(value instanceof BSONObject){
						if(value instanceof BasicBSONList){
							
							BasicBSONList list = (BasicBSONList)value;
							ComparativeBaseList comparativeList = null;
							if(isMulti){
								if(and){
									comparativeList = new ComparativeAND();
								}else{
									comparativeList = new ComparativeOR();
								}
							}else{
								comparativeList = new ComparativeAND();
							}
							
							for(Object object : list){
								if(object instanceof BSONObject ){
									int start = stack.size();
									toComparative(parameterMap,stack,(BSONObject)object,table);
									int end = stack.size();
									if(end > start){
										comparable = (Comparative)stack.pop();
										comparativeList.addComparative(comparable);
									}
								}else{
									comparativeList.addComparative(new Comparative(Comparative.Equivalent,(Comparable)object));
								}
							}
							
							if(comparativeList.getList().size()>0){
								if(column != null){
									putToColumnMap(parameterMap,column,comparativeList);
								}else{
									stack.push(comparativeList);
								}
							}
							
						}else{
							int start = stack.size();
							toComparative(parameterMap,stack,(BSONObject)value,table);
							int end = stack.size();
							if(end > start){
								comparable = (Comparative)stack.pop();
								if(column != null){
									putToColumnMap(parameterMap,column,comparable);
								}else{
									stack.push(comparable);
								}
							}
						}
					}
					// value is constant
					else{
						if(value instanceof byte[]){
							value = new String((byte[]) value);
						}else if(value instanceof BSONTimestamp){
							value =  new Date( ((BSONTimestamp)value).getTime() * 1000L );
						}else if(value instanceof java.util.regex.Pattern){
							value = ((java.util.regex.Pattern)value).pattern();
							//ignore 
							continue;
						}
						//put to map or push to stack
						comparable = new Comparative(comparativeValue,(Comparable)value);
						if(column != null){
							putToColumnMap(parameterMap,column,comparable);
						}else{
							stack.push(comparable);
						}
					}
				}
				
				int end = stack.size();
				if(end - current > 1){
					ComparativeBaseList comparativeList = new ComparativeAND();
					for(int i = end-current;i>0;i--){
						comparativeList.addComparative((Comparative)stack.pop());
					}
					stack.push(comparativeList);
				}
			}
		}
		
	}
	
	
	public static void main(String[] args){
		String lines[] = new String[]{
				"{ 'SDID' : 1 , '$or' : [ { 'DEL_FLAG' : { '$ne' : 0}} , { 'RESERVE' : { '$in' : [ 1 , 2 , 3]}}]}",
				"{ '$or' : [ { 's' : 281} , { 's' : 28}]}",
				"{'a': { '$all': [ 2, 3, 4 ] } }",
				"{ 'field' : { '$gt': 1, '$lt': 12 } }",
				"{ 'x' : 3 ,  'z' : 1 }",
				"{'j':{'$in': [2,4,6]}}",
				"{'j':{'$nin': [2,4,6]}}"
		};
		for(String line : lines){
			BSONObject object = (BSONObject)JSON.parse(line);
			Map<Column, Comparative> parameterMap = new HashMap<Column, Comparative>();
			toComparative(parameterMap,new Stack(),object,null);
			System.out.println(parameterMap);
		}
	}
}
