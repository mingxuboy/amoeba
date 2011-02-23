package com.meidusa.amoeba.aladdin.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.handler.PreparedStatmentInfo;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.ExecutePacket;
import com.meidusa.amoeba.mysql.net.packet.LongDataPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.statement.PropertyStatement;
import com.meidusa.amoeba.parser.statement.SelectStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.SqlBaseQueryRouter;
import com.meidusa.amoeba.route.SqlQueryObject;
import com.meidusa.amoeba.util.ByteUtil;
import com.meidusa.amoeba.util.StringFillFormat;

/**
 * @author struct
 * @author hexianmao
 */
public class AladdinMessageDispatcher implements MessageHandler {

    private static Logger logger     = Logger.getLogger(AladdinMessageDispatcher.class);

    private static long   timeout    = -1;
    private static byte[] STATIC_OK_BUFFER;
    private static int    fillLength = 18;

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
	
	        try {
	            if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_QUIT)) {
	                if (logger.isDebugEnabled()) {
	                    logger.debug("COM_QUIT command");
	                }
	                return;
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_CLOSE)) {
	                if (logger.isDebugEnabled()) {
	                    logger.debug("COM_STMT_CLOSE command");
	                }
	                return;
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_PING)) {
	                if (logger.isDebugEnabled()) {
	                    logger.debug("COM_PING command");
	                }
	                conn.postMessage(STATIC_OK_BUFFER);
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_QUERY)) {
	                QueryCommandPacket packet = new QueryCommandPacket();
	                packet.init(message, connection);
	                if (logger.isDebugEnabled()) {
	                    logger.debug(StringFillFormat.format("COM_QUERY:", fillLength) + packet);
	                }
	
	                SqlBaseQueryRouter router = (SqlBaseQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
	                SqlQueryObject queryObject = new SqlQueryObject();
	                queryObject.isPrepared = false;
	                queryObject.sql = packet.query;
	               
	                ObjectPool[] pools = router.doRoute(conn, queryObject);
	                Statement statment = router.parseStatement(conn, packet.query);
	                if (statment != null && statment instanceof SelectStatement && ((SelectStatement)statment).isQueryLastInsertId()) {
            			List<RowDataPacket> list = new ArrayList<RowDataPacket>();
            			RowDataPacket row = new RowDataPacket(false);
            			row.columns = new ArrayList<Object>();
            			row.columns.add(conn.getLastInsertId());
            			list.add(row);
            			conn.lastPacketResult.setRowList(list);
            			conn.lastPacketResult.wirteToConnection(conn);
            			return;
	                }
	                
	                if(pools == null || statment instanceof PropertyStatement){
	                	conn.postMessage(STATIC_OK_BUFFER);
	                	return;
	                }
	                
	                MessageHandler handler = new QueryCommandMessageHandler(conn, packet.query, null, pools, timeout);
	                if (handler instanceof Sessionable) {
	                    Sessionable session = (Sessionable) handler;
	                    try {
	                        session.startSession();
	                    } catch (Exception e) {
	                        logger.error("start Session error:", e);
	                        throw e;
	                    } finally {
	                        session.endSession(false);
	                    }
	                }
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_PREPARE)) {
	                QueryCommandPacket packet = new QueryCommandPacket();
	                packet.init(message, connection);
	                if (logger.isDebugEnabled()) {
	                    logger.debug(StringFillFormat.format("COM_STMT_PREPARE:", fillLength) + packet);
	                }
	
	                PreparedStatmentInfo pInfo = conn.getPreparedStatmentInfo(packet.query);
	                byte[] buffer = pInfo.getByteBuffer();
	                conn.postMessage(buffer);
	                return;
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_SEND_LONG_DATA)) {
	                if (logger.isDebugEnabled()) {
	                    logger.debug("COM_STMT_SEND_LONG_DATA");
	                }
	                conn.addLongData(message);
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_EXECUTE)) {
	                long statmentId = ExecutePacket.readStatmentID(message);
	                PreparedStatmentInfo pInfo = conn.getPreparedStatmentInfo(statmentId);
	
	                if (pInfo == null) {
	                    logger.error("Unknown prepared statment id:" + statmentId);
	
	                    ErrorPacket error = new ErrorPacket();
	                    error.errno = 1044;
	                    error.packetId = 1;
	                    error.sqlstate = "42000";
	                    error.serverErrorMessage = "Unknown prepared statment id=" + statmentId;
	                    conn.postMessage(error.toByteBuffer(connection).array());
	                } else {
	                    String sql = pInfo.getSql();
	                    if (logger.isDebugEnabled()) {
	                        logger.debug(StringFillFormat.format("COM_STMT_EXECUTE:", fillLength) + "sql[" + sql.trim() + "]");
	                        logger.debug(StringFillFormat.format("COM_STMT_EXECUTE:", fillLength) + "params[" + pInfo.getParameterCount() + "]");
	                        logger.debug(StringFillFormat.format("COM_STMT_EXECUTE:", fillLength) + "dump bytes[" + ByteUtil.toHex(message, 0, message.length) + "]");
	                    }
	                    Map<Integer, Object> longMap = null;
	                    if (conn.getLongDataList().size() > 0) {
	                        longMap = new HashMap<Integer, Object>();
	                        for (byte[] longdate : conn.getLongDataList()) {
	                            LongDataPacket packet = new LongDataPacket();
	                            packet.init(longdate, connection);
	                            longMap.put(packet.parameterIndex, packet.data);
	                        }
	                        conn.clearLongData();
	                    }
	                    ExecutePacket packet = new ExecutePacket(pInfo, longMap);
	                    packet.init(message, connection);
	                    if (logger.isDebugEnabled()) {
	                        logger.debug(StringFillFormat.format("COM_STMT_EXECUTE:", fillLength) + "[" + packet + "]");
	                    }
	                    SqlBaseQueryRouter router = (SqlBaseQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
	                    
	                    SqlQueryObject queryObject = new SqlQueryObject();
		                queryObject.isPrepared = true;
		                queryObject.sql = sql;
		                queryObject.parameters = packet.getParameters();
		                ObjectPool[] pools = router.doRoute(conn, queryObject);
		                
	                    PreparedStatmentExecuteMessageHandler handler = new PreparedStatmentExecuteMessageHandler(conn, pInfo, packet, pools, timeout);
	                    if (handler instanceof Sessionable) {
	                        Sessionable session = (Sessionable) handler;
	                        try {
	                            session.startSession();
	                        } catch (Exception e) {
	                            logger.error("start Session error:", e);
	                            throw e;
	                        } finally {
	                            session.endSession(false);
	                        }
	                    }
	                }
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_INIT_DB)) {
	                QueryCommandPacket packet = new QueryCommandPacket();
	                packet.init(message, connection);
	                if (logger.isDebugEnabled()) {
	                    logger.debug(StringFillFormat.format("COM_INIT_DB:", fillLength) + packet);
	                }
	                conn.setSchema(packet.query);
	                conn.postMessage(STATIC_OK_BUFFER);
	            } else {
	                QueryCommandPacket packet = new QueryCommandPacket();
	                packet.init(message, connection);
	                logger.error("Aladdin unsupport packet:" + packet);
	
	                ErrorPacket error = new ErrorPacket();
	                error.errno = 1044;
	                error.packetId = 1;
	                error.sqlstate = "42000";
	                error.serverErrorMessage = "can not use this command here!!";
	                conn.postMessage(error.toByteBuffer(connection).array());
	            }
	        } catch (Exception e) {
	            logger.error("Aladdin dispatch message error", e);
	
	            ErrorPacket error = new ErrorPacket();
	            error.errno = 1044;
	            error.packetId = 1;
	            error.sqlstate = "42000";
	            error.serverErrorMessage = e.getMessage();
	            conn.postMessage(error.toByteBuffer(connection).array());
	        }
	    }
    }

}
