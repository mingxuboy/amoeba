/**
 * <pre>
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.mysql.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
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
import com.meidusa.amoeba.parser.statment.SelectStatment;
import com.meidusa.amoeba.parser.statment.Statment;
import com.meidusa.amoeba.route.QueryRouter;
import com.meidusa.amoeba.util.Tuple;

/**
 * handler
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class MySqlCommandDispatcher implements MessageHandler {

    protected static Logger logger  = Logger.getLogger(MySqlCommandDispatcher.class);

    private static long     timeout = -1;

    private static byte[]   STATIC_OK_BUFFER;
    static {
        OkPacket ok = new OkPacket();
        ok.affectedRows = 0;
        ok.insertId = 0;
        ok.packetId = 1;
        ok.serverStatus = 2;
        STATIC_OK_BUFFER = ok.toByteBuffer(null).array();
    }

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
	            if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_QUIT) || MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_CLOSE)) {
	                if (logger.isDebugEnabled()) {
	                    logger.debug(command);
	                }
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_PING)) {
	                conn.postMessage(STATIC_OK_BUFFER);
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_QUERY)) {
	                QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
	                Tuple<Statment,ObjectPool[]> tuple = router.doRoute(conn, command.query, false, null);
	                Statment statment = tuple.left;
	                ObjectPool[] pools = tuple.right;
	                if (statment != null && statment instanceof SelectStatment && ((SelectStatment)tuple.left).isQueryLastInsertId()) {
	                	MysqlResultSetPacket lastPacketResult = createLastInsertIdPacket(conn,(SelectStatment)statment);
            			lastPacketResult.wirteToConnection(conn);
            			return;
	                }
	                
	                if(pools == null){
	                	conn.postMessage(STATIC_OK_BUFFER);
	                	return;
	                }
	                
	                MessageHandler handler = new QueryCommandMessageHandler(conn, message,statment, tuple.right, timeout);
	                if (handler instanceof Sessionable) {
	                    Sessionable session = (Sessionable) handler;
	                    try {
	                        session.startSession();
	                    } catch (Exception e) {
	                        logger.error("start Session error:", e);
	                        session.endSession();
	                        throw e;
	                    }
	                }
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_PREPARE)) {
	                PreparedStatmentInfo preparedInf = conn.getPreparedStatmentInfo(command.query);
	                byte[] buffer = preparedInf.getByteBuffer();
	                conn.postMessage(buffer);
	                return;
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_SEND_LONG_DATA)) {
	                conn.addLongData(message);
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_EXECUTE)) {
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
	                	Statment statment = preparedInf.getStatment();
	                	if (statment != null && statment instanceof SelectStatment && ((SelectStatment)statment).isQueryLastInsertId()) {
	                		MysqlResultSetPacket lastPacketResult = createLastInsertIdPacket(conn,(SelectStatment)statment);
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
	
	                    QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
	                    Tuple<Statment,ObjectPool[]> tuple = router.doRoute(conn, preparedInf.getPreparedStatment(), false, executePacket.getParameters());
	
	                    PreparedStatmentExecuteMessageHandler handler = new PreparedStatmentExecuteMessageHandler(
	                                                                                                              conn,
	                                                                                                              preparedInf,tuple.left,
	                                                                                                              message,
	                                                                                                              tuple.right,
	                                                                                                              timeout);
	                    if (handler instanceof Sessionable) {
	                        Sessionable session = (Sessionable) handler;
	                        try {
	                            session.startSession();
	                        } catch (Exception e) {
	                            logger.error("start Session error:", e);
	                            session.endSession();
	                            throw e;
	                        }
	                    }
	                }
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_INIT_DB)) {
	                conn.setSchema(command.query);
	                conn.postMessage(STATIC_OK_BUFFER);
	            } else {
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
	            conn.postMessage(error.toByteBuffer(connection).array());
	            logger.error("messageDispate error", e);
	        }
		}
    }
    
    private MysqlResultSetPacket createLastInsertIdPacket(MysqlClientConnection conn,SelectStatment statment){
    	Map<String,Column> selectedMap = ((SelectStatment)statment).getSelectColumnMap();
		MysqlResultSetPacket lastPacketResult = new MysqlResultSetPacket(null);
		lastPacketResult.resulthead = new ResultSetHeaderPacket();
		lastPacketResult.resulthead.columns = selectedMap.size();
		RowDataPacket row = new RowDataPacket(false);
		row.columns = new ArrayList<Object>();
		int index =0; 
		for(Map.Entry<String, Column> entry : selectedMap.entrySet()){
			String alias = entry.getValue().getAlias();
			if("LAST_INSERT_ID".equalsIgnoreCase(entry.getValue().getName()) || "@@IDENTITY".equalsIgnoreCase(entry.getValue().getName())){
				row.columns.add(conn.getLastInsertId());
			}else{
				row.columns.add(0);
			}
			lastPacketResult.fieldPackets = new FieldPacket[selectedMap.size()];
			FieldPacket field = new FieldPacket();
			field.type = MysqlDefs.FIELD_TYPE_LONGLONG;
			field.name = (alias == null?entry.getValue().getName():alias);
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
