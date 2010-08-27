package com.meidusa.amoeba.mongodb.route;

import java.util.Map;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.dbobject.Schema;
import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.BaseQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;

public class MongodbQueryRouter extends BaseQueryRouter<MongodbClientConnection,RequestMongodbPacket> {

	@Override
	protected Map<Table, Map<Column, Comparative>> evaluateStatement(
			Statement statement, RequestMongodbPacket queryObject) {
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
		
		if(queryObject instanceof QueryMongodbPacket){
			QueryMongodbPacket query = (QueryMongodbPacket)queryObject;
			BSONObject bson = query.query;
			if(bson != null){
				Map map = bson.toMap();
				if(map != null){
					for(Object item : map.entrySet()){
						Map.Entry entry = (Map.Entry)item;
						String name = (String)entry.getKey();
						Object value =  entry.getValue();
					}
				}
				
			}
		}
		return null;
	}

	@Override
	public Statement parseStatement(MongodbClientConnection conn, RequestMongodbPacket queryObject) {
		if(queryObject instanceof QueryMongodbPacket){
			
		}
		return null;
	}

	@Override
	protected void setConnectionPropertiesWithStatement(MongodbClientConnection connection,
			Statement statement, RequestMongodbPacket queryObject) {
		
	}

}
