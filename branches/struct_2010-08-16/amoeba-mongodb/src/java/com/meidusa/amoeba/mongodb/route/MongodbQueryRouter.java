package com.meidusa.amoeba.mongodb.route;

import java.util.HashMap;
import java.util.Map;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.dbobject.Schema;
import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.route.AbstractQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;

public class MongodbQueryRouter extends AbstractQueryRouter<MongodbClientConnection,RequestMongodbPacket> {

	@Override
	protected Map<Table, Map<Column, Comparative>> evaluateTable(MongodbClientConnection connection,RequestMongodbPacket queryObject) {
		Table table = new Table();
		if(queryObject.fullCollectionName != null){
			int index = queryObject.fullCollectionName.indexOf(".");
			if(index >0){
				String schemaName = queryObject.fullCollectionName.substring(0,index);
				String tableBame =  queryObject.fullCollectionName.substring(index +1);
				table.setName(tableBame);
				Schema schema = new Schema();
				schema.setName(schemaName);
				table.setSchema(schema);
			}else{
				table.setName(queryObject.fullCollectionName);
			}
		}
		BSONObject bson = null;
		if(queryObject instanceof QueryMongodbPacket){
			QueryMongodbPacket query = (QueryMongodbPacket)queryObject;
			bson = query.query;
		}
		else if(queryObject  instanceof InsertMongodbPacket){
			InsertMongodbPacket query = (InsertMongodbPacket)queryObject;
			bson = query.documents.get(0);
		}else if(queryObject  instanceof DeleteMongodbPacket){
			DeleteMongodbPacket query = (DeleteMongodbPacket)queryObject;
			bson =  query.selector;
		}
		
		if(bson != null){
			Map map = bson.toMap();
			if(map != null && map.size() >0){
				Map<Column, Comparative> parameterMap = new HashMap<Column, Comparative>();
				Map<Table, Map<Column, Comparative>> tableMap = new HashMap<Table, Map<Column, Comparative>>();
				tableMap.put(table, parameterMap);
				for(Object item : map.entrySet()){
					Map.Entry entry = (Map.Entry)item;
					String name = (String)entry.getKey();
					Object value =  entry.getValue();
					Column column = new Column();
					column.setName(name);
					column.setTable(table);
					Comparative comparable = new Comparative(Comparative.Equivalent,(Comparable)value);
					parameterMap.put(column, comparable);
				}
				return tableMap;
			}
		}
		return null;
	}

}
