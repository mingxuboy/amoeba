/*
 * Copyright 2008-2108 amoeba.meidusa.com 
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
package com.meidusa.amoeba.mysql.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.context.MysqlRuntimeContext;
import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.BindValue;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.ExecutePacket;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.LongDataPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;
import com.meidusa.amoeba.mysql.net.packet.result.MysqlResultSetPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.statement.SelectStatement;
import com.meidusa.amoeba.parser.statement.ShowStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.SqlBaseQueryRouter;
import com.meidusa.amoeba.route.SqlQueryObject;

/**
 * handler
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class MySqlCommandDispatcher implements MessageHandler {

    protected static Logger logger  = Logger.getLogger(MySqlCommandDispatcher.class);
    private long timeout = ProxyRuntimeContext.getInstance().getRuntimeContext().getQueryTimeout() * 1000;

    private static byte[]   STATIC_OK_BUFFER;
    static {
        OkPacket ok = new OkPacket();
        ok.affectedRows = 0;
        ok.insertId = 0;
        ok.packetId = 1;
        ok.serverStatus = 2;
        STATIC_OK_BUFFER = ok.toByteBuffer(null).array();
    }

    /**
     * Ping 、COM_STMT_SEND_LONG_DATA command remove to @MysqlClientConnection #doReceiveMessage()
     */
    public void handleMessage(Connection connection) {
    	
    	byte[] message = null;
		while((message = connection.getInQueue().getNonBlocking()) != null){
	        MysqlClientConnection conn = (MysqlClientConnection) connection;
	
	        QueryCommandPacket command = new QueryCommandPacket();
	        command.init(message, connection);
	        if (logger.isDebugEnabled()) {
	            logger.debug(command.query);
	        }
	        try {
	            if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_QUERY)) {
	            	
	            	SqlBaseQueryRouter router = (SqlBaseQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
	            	Statement statement = router.parseStatement(conn, command.query);

	            	if(command.query != null && (command.query.indexOf("'$version'")>0 || command.query.indexOf("@amoebaversion")>0)){
	            		MysqlResultSetPacket lastPacketResult = createAmoebaVersion(conn,(SelectStatement)statement,false);
            			lastPacketResult.wirteToConnection(conn);
            			return;
	            	}
	            	
	                SqlQueryObject queryObject = new SqlQueryObject();
	                queryObject.isPrepared = false;
	                queryObject.sql = command.query;
	               
	                ObjectPool[] pools = router.doRoute(conn, queryObject);
	               
		                
	                if (statement != null && statement instanceof SelectStatement && ((SelectStatement)statement).isQueryLastInsertId()) {
	                	MysqlResultSetPacket lastPacketResult = createLastInsertIdPacket(conn,(SelectStatement)statement,false);
            			lastPacketResult.wirteToConnection(conn);
            			return;
	                }
	                
	                if(statement instanceof ShowStatement){
	                	if(pools != null && pools.length>1){
	                		pools = new ObjectPool[]{pools[0]};
	                	}
	                }
	                
	                if(pools == null){
	                	conn.postMessage(STATIC_OK_BUFFER);
	                	return;
	                }
	                
	                MessageHandler handler = new QueryCommandMessageHandler(conn, message,statement, pools, timeout);
	                if (handler instanceof Sessionable) {
	                    Sessionable session = (Sessionable) handler;
	                    try {
	                        session.startSession();
	                    } catch (Exception e) {
	                        logger.error("start Session error:", e);
	                        session.endSession(true);
	                        throw e;
	                    }
	                }
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_PREPARE)) {
	            	
	            	/**
	            	 * 获取之前prepared过的数据，直接返回给客户端，如果没有则需要往后端mysql发起请求，
	            	 * 然后数据以后填充PreparedStatmentInfo，并且给客户端
	            	 */
	                PreparedStatmentInfo preparedInf = conn.getPreparedStatmentInfo(command.query);
	                if(preparedInf.getByteBuffer() != null && preparedInf.getByteBuffer().length >0){
	                	conn.postMessage(preparedInf.getByteBuffer());
	                	return;
	                }
	                
	                /**
	                 * 无命中情况
	                 */
	                SqlBaseQueryRouter router = (SqlBaseQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
	                SqlQueryObject queryObject = new SqlQueryObject();
	                queryObject.isPrepared = true;
	                queryObject.sql = command.query;
	               
	                ObjectPool[] pools = router.doRoute(conn, queryObject);
	                Statement statment = router.parseStatement(conn, command.query);
	                
	            	PreparedStatmentMessageHandler handler = new PreparedStatmentMessageHandler(conn,preparedInf,statment, message , new ObjectPool[]{pools[0]}, timeout);
	            	if (handler instanceof Sessionable) {
	                    Sessionable session = (Sessionable) handler;
	                    try {
	                        session.startSession();
	                    } catch (Exception e) {
	                        logger.error("start Session error:", e);
	                        session.endSession(true);
	                        throw e;
	                    }
	                }
	                return;
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_EXECUTE)) {
	            	
	            	try{
		                long statmentId = ExecutePacket.readStatmentID(message);
		                PreparedStatmentInfo preparedInf = conn.getPreparedStatmentInfo(statmentId);
		                if (preparedInf == null) {
		                    ErrorPacket error = new ErrorPacket();
		                    error.errno = 1044;
		                    error.packetId = 1;
		                    error.sqlstate = "42000";
		                    error.serverErrorMessage = "Unknown prepared statment id=" + statmentId;
		                    conn.postMessage(error.toByteBuffer(connection).array());
		                    logger.warn("Unknown prepared statment id:" + statmentId);
		                } else {
		                	Statement statment = preparedInf.getStatment();
		                	if (statment != null && statment instanceof SelectStatement && ((SelectStatement)statment).isQueryLastInsertId()) {
		                		MysqlResultSetPacket lastPacketResult = createLastInsertIdPacket(conn,(SelectStatement)statment,true);
		            			lastPacketResult.wirteToConnection(conn);
		            			return;
			                }
		                	
		                    Map<Integer, Object> longMap = new HashMap<Integer, Object>();
		                    for (byte[] longdate : conn.getLongDataList()) {
		                        LongDataPacket packet = new LongDataPacket();
		                        packet.init(longdate, connection);
		                        longMap.put(packet.parameterIndex, packet.data);
		                    }
		
		                    ExecutePacket executePacket = new ExecutePacket(preparedInf, longMap);
		                    executePacket.init(message, connection);
		
		                    SqlBaseQueryRouter router = (SqlBaseQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
			                SqlQueryObject queryObject = new SqlQueryObject();
			                queryObject.isPrepared = false;
			                queryObject.sql = preparedInf.getSql();
			                queryObject.parameters = executePacket.getParameters();
			                ObjectPool[] pools = router.doRoute(conn, queryObject);
		
		                    PreparedStatmentExecuteMessageHandler handler = new PreparedStatmentExecuteMessageHandler(
		                                                                                                              conn,
		                                                                                                              preparedInf,statment,
		                                                                                                              message,
		                                                                                                              pools,
		                                                                                                              timeout);
		                    handler.setExecutePacket(executePacket);
		                    if (handler instanceof Sessionable) {
		                        Sessionable session = (Sessionable) handler;
		                        try {
		                            session.startSession();
		                        } catch (Exception e) {
		                            logger.error("start Session error:", e);
		                            session.endSession(true);
		                            throw e;
		                        }
		                    }
		                }
	            	}finally{
	            		conn.clearLongData();
	            	}
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_INIT_DB)) {
	                conn.setSchema(command.query);
	                conn.postMessage(STATIC_OK_BUFFER);
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_CHANGE_USER)){
	            	conn.postMessage(STATIC_OK_BUFFER);
	            }else{
	                ErrorPacket error = new ErrorPacket();
	                error.errno = 1044;
	                error.packetId = 1;
	                error.sqlstate = "42000";
	                error.serverErrorMessage = "can not use this command here!!";
	                conn.postMessage(error.toByteBuffer(connection).array());
	                logger.warn("unsupport packet:" + command);
	            }
	        } catch (Exception e) {
	            ErrorPacket error = new ErrorPacket();
	            error.errno = 1044;
	            error.packetId = 1;
	            error.sqlstate = "42000";
	            error.serverErrorMessage = e.getMessage();
	            e.printStackTrace();
	            conn.postMessage(error.toByteBuffer(connection).array());
	            logger.error("messageDispate error", e);
	        }
		}
    }
    
    private MysqlResultSetPacket createAmoebaVersion(MysqlClientConnection conn,SelectStatement statment,boolean isPrepared){
    	Map<String,Column> selectedMap = ((SelectStatement)statment).getSelectColumnMap();
		MysqlResultSetPacket lastPacketResult = new MysqlResultSetPacket(null);
		lastPacketResult.resulthead = new ResultSetHeaderPacket();
		lastPacketResult.resulthead.columns = (selectedMap.size()==0?1:selectedMap.size());
		if(selectedMap.size() == 0){
			Column column = new Column();
			column.setName("@amoebaversion");
			selectedMap.put("@amoebaversion", column);
		}
		lastPacketResult.resulthead.extra = 1;
		RowDataPacket row = new RowDataPacket(isPrepared);
		row.columns = new ArrayList<Object>();
		int index =0; 
		lastPacketResult.fieldPackets = new FieldPacket[selectedMap.size()];
		for(Map.Entry<String, Column> entry : selectedMap.entrySet()){
			FieldPacket field = new FieldPacket();
			String alias = entry.getValue().getAlias();
			
			
			if("@amoebaversion".equalsIgnoreCase(entry.getValue().getName()) 
				||  "'$version'".equalsIgnoreCase(entry.getValue().getName())){
				BindValue value = new BindValue();
				value.bufferType = MysqlDefs.FIELD_TYPE_VARCHAR;
				value.value = MysqlRuntimeContext.SERVER_VERSION;
				value.scale = 20;
				value.isSet = true;
				row.columns.add(value);
				field.name = (alias == null?entry.getValue().getName()+"()":alias);
			}else{
				BindValue value = new BindValue();
				value.bufferType = MysqlDefs.FIELD_TYPE_VARCHAR;
				value.scale = 20;
				value.isNull = true;
				row.columns.add(value);
				field.name = (alias == null?entry.getValue().getName():alias);
			}
			
			field.type = (byte)MysqlDefs.FIELD_TYPE_VARCHAR;
			field.catalog = "def";
			field.length = 20;
			lastPacketResult.fieldPackets[index] = field; 
			index++;
		}
			
		List<RowDataPacket> list = new ArrayList<RowDataPacket>();
		list.add(row);
		lastPacketResult.setRowList(list);
		return lastPacketResult;
    }
    
    private MysqlResultSetPacket createLastInsertIdPacket(MysqlClientConnection conn,SelectStatement statment,boolean isPrepared){
    	Map<String,Column> selectedMap = ((SelectStatement)statment).getSelectColumnMap();
		MysqlResultSetPacket lastPacketResult = new MysqlResultSetPacket(null);
		lastPacketResult.resulthead = new ResultSetHeaderPacket();
		lastPacketResult.resulthead.columns = selectedMap.size();
		lastPacketResult.resulthead.extra = 1;
		RowDataPacket row = new RowDataPacket(isPrepared);
		row.columns = new ArrayList<Object>();
		int index =0; 
		lastPacketResult.fieldPackets = new FieldPacket[selectedMap.size()];
		for(Map.Entry<String, Column> entry : selectedMap.entrySet()){
			FieldPacket field = new FieldPacket();
			String alias = entry.getValue().getAlias();
			
			
			if("LAST_INSERT_ID".equalsIgnoreCase(entry.getValue().getName())){
				BindValue value = new BindValue();
				value.bufferType = MysqlDefs.FIELD_TYPE_LONGLONG;
				value.longBinding = conn.getLastInsertId();
				value.scale = 20;
				value.isSet = true;
				row.columns.add(value);
				field.name = (alias == null?entry.getValue().getName()+"()":alias);
				
			}else if("@@IDENTITY".equalsIgnoreCase(entry.getValue().getName())){

				BindValue value = new BindValue();
				value.bufferType = MysqlDefs.FIELD_TYPE_LONGLONG;
				value.longBinding = conn.getLastInsertId();
				value.scale = 20;
				value.isSet = true;
				row.columns.add(value);
				
				row.columns.add(value);
				field.name = (alias == null?entry.getValue().getName():alias);
				
			}else{
				BindValue value = new BindValue();
				value.bufferType = MysqlDefs.FIELD_TYPE_STRING;
				value.scale = 20;
				value.isNull = true;
				row.columns.add(value);
				field.name = (alias == null?entry.getValue().getName():alias);
			}
			
			field.type = MysqlDefs.FIELD_TYPE_LONGLONG;
			field.catalog = "def";
			field.length = 20;
			lastPacketResult.fieldPackets[index] = field; 
			index++;
		}
			
		List<RowDataPacket> list = new ArrayList<RowDataPacket>();
		list.add(row);
		lastPacketResult.setRowList(list);
		return lastPacketResult;
    }
}
