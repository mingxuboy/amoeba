package com.meidusa.amoeba.mongodb.route;

import java.util.Map;

import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.BaseQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;

public class MongodbQueryRouter extends BaseQueryRouter<MongodbClientConnection,QueryMongodbPacket> {

	@Override
	protected Map<Table, Map<Column, Comparative>> evaluateStatement(
			Statement statment, QueryMongodbPacket queryObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Statement parseStatement(MongodbClientConnection conn, QueryMongodbPacket queryObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setConnectionPropertiesWithStatement(MongodbClientConnection connection,
			Statement statment, QueryMongodbPacket queryObject) {
		// TODO Auto-generated method stub
		
	}

}
