/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mysql.handler;


import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.ExecutePacket;
import com.meidusa.amoeba.mysql.net.packet.LongDataPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.route.QueryRouter;

/**
 * handler
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class MySqlCommandDispatcher implements MessageHandler {
	private static long timeout = -1;
	protected static Logger logger = Logger.getLogger(MySqlCommandDispatcher.class);
	private static byte[] STATIC_OK_BUFFER;
	static{
		OkPacket ok = new OkPacket();
		ok.affectedRows = 0;
		ok.insertId = 0;
		ok.packetId = 1;
		ok.serverStatus = 2;
		STATIC_OK_BUFFER = ok.toByteBuffer(null).array();
	}
	
	public void handleMessage(Connection connection,byte[] message) {
		MysqlClientConnection conn = (MysqlClientConnection)connection;
		
		QueryCommandPacket command = new QueryCommandPacket();
		command.init(message,connection);
		try {
			if(MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_QUIT) || MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_CLOSE)){
				if(logger.isDebugEnabled()){
					logger.debug(command);
				}
			}else if(MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_PING)){
				conn.postMessage(STATIC_OK_BUFFER);
			}else if(MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_QUERY)){
				
				QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
				ObjectPool[] pools = router.doRoute(conn,command.arg,false,null);
				if(pools == null){
					conn.postMessage(STATIC_OK_BUFFER);
					return;
				}
				MessageHandler handler = new QueryCommandMessageHandler(conn,message,pools,timeout);
				if(handler instanceof Sessionable){
					Sessionable session = (Sessionable)handler;
					try{
						session.startSession();
					}catch(Exception e){
						logger.error("start Session error:",e);
						session.endSession();
						throw e;
					}
				}
			}else if(MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_PREPARE)){
				
				PreparedStatmentInfo preparedInf = conn.getPreparedStatmentInfo(command.arg);
				List<byte[]> list = preparedInf.getPreparedStatmentBuffers();
				for(byte[] buffer : list){
					conn.postMessage(buffer);
				}
				return;
			}else if(MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_SEND_LONG_DATA)){
				conn.addLongData(message);
			}else if(MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_EXECUTE)){
				long statmentId = ExecutePacket.readStatmentID(message);
				PreparedStatmentInfo preparedInf = conn.getPreparedStatmentInfo(statmentId);
				
				if(preparedInf == null){
					ErrorPacket error = new ErrorPacket();
					error.errno = 1044;
					error.packetId = 1;
					error.sqlstate = "42000";
					error.serverErrorMessage ="Unknown prepared statment id="+statmentId;
					conn.postMessage(error.toByteBuffer(connection).array());
					logger.warn("Unknown prepared statment id:"+statmentId);
				}else{
					Map<Integer,Object> longMap = new HashMap<Integer,Object>();
					for(byte[] longdate:conn.getLongDataList()){
						LongDataPacket packet = new LongDataPacket();
						packet.init(longdate,connection);
						longMap.put(packet.parameterIndex, packet.data);
					}
					
					ExecutePacket executePacket = new ExecutePacket(preparedInf.getParameterCount(),longMap);
					executePacket.init(message,connection);

					QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
					ObjectPool[] pools = router.doRoute(conn,preparedInf.getPreparedStatment(),false,executePacket.getParameters());
					
					PreparedStatmentExecuteMessageHandler handler = new PreparedStatmentExecuteMessageHandler(conn,preparedInf,message,pools,timeout);
					if(handler instanceof Sessionable){
						Sessionable session = (Sessionable)handler;
						try{
							session.startSession();
						}catch(Exception e){
							logger.error("start Session error:",e);
							session.endSession();
							throw e;
						}
					}
				}
			}else if(MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_INIT_DB)){
				conn.setSchema(command.arg);
				conn.postMessage(STATIC_OK_BUFFER);
				
			}else{
				ErrorPacket error = new ErrorPacket();
				error.errno = 1044;
				error.packetId = 1;
				error.sqlstate = "42000";
				error.serverErrorMessage ="can not use this command here!!";
				conn.postMessage(error.toByteBuffer(connection).array());
				logger.debug("unsupport packet:"+command);
			}
		} catch (Exception e) {
			logger.error("messageDispate error", e);
			ErrorPacket error = new ErrorPacket();
			error.errno = 1044;
			error.packetId = 1;
			error.sqlstate = "42000";
			error.serverErrorMessage =e.getMessage();
			conn.postMessage(error.toByteBuffer(connection).array());
		}
		
	}
}