package com.meidusa.amoeba.mongodb.route;

import java.util.Map;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.BaseQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;

public class MongodbQueryRouter extends BaseQueryRouter<MongodbClientConnection,RequestMongodbPacket> {

	@Override
	protected Map<Table, Map<Column, Comparative>> evaluateStatement(
			Statement statement, RequestMongodbPacket queryObject) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		
	}

}
