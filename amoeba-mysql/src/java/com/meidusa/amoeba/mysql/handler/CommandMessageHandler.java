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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.handler.session.CommandStatus;
import com.meidusa.amoeba.mysql.handler.session.ConnectionStatuts;
import com.meidusa.amoeba.mysql.handler.session.SessionStatus;
import com.meidusa.amoeba.mysql.net.CommandInfo;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.MysqlConnection;
import com.meidusa.amoeba.mysql.net.MysqlServerConnection;
import com.meidusa.amoeba.mysql.net.packet.CommandPacket;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.net.packet.PacketBuffer;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.parser.statement.CallStatement;
import com.meidusa.amoeba.parser.statement.DMLStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class CommandMessageHandler implements MessageHandler,Sessionable,Reporter.SubReporter {
	static Logger logger = Logger.getLogger(CommandMessageHandler.class); 
	
	protected MysqlClientConnection source;
	private boolean completed;
	private long createTime;
	private long timeout;
	private long endTime;
	private boolean ended = false;
	protected CommandQueue commandQueue;
	private boolean forceEnded =  false; 
	private ObjectPool[] pools;
	private CommandInfo info = new CommandInfo();
	protected byte commandType;
	protected Map<Connection,MessageHandler> handlerMap = Collections.synchronizedMap(new HashMap<Connection,MessageHandler>());
	private PacketBuffer buffer = new AbstractPacketBuffer(10240);
	private boolean started;
	private long lastTimeMillis = System.currentTimeMillis();
	private ErrorPacket errorPacket;
	protected Statement statment;
	private QueryCommandPacket command = new QueryCommandPacket();
	public CommandMessageHandler(final MysqlClientConnection source,byte[] query,Statement statment, ObjectPool[] pools,long timeout){
		commandQueue = new CommandQueue(source,statment);
		command.init(query,source);
		this.pools = pools;
		info.setBuffer(query);
		info.setMain(true);
		this.statment = statment;
		this.source = source;
		this.createTime = System.currentTimeMillis();
		this.timeout = timeout;
	}
	
	public boolean isMultiplayer(){
		return commandQueue.isMultiple();
	}
	/**
	 * 判断被handled的Connection 消息传送是否都完成
	 * @return
	 */
	public boolean isCompleted(){
		return completed;
	}
	
	/**
	 * 主要是为了服务端连接 与 客户端连接的环境一致（比如，当前的schema 、charset等）
	 * 
	 * 在发送主命令之前，预先需要发送一些额外的命令，比如sourceConnection、destConnection 当前的database不一致，需要发送init_db Command
	 * 为了减少复杂度，只要一个Connection需要发送命令，那么所有连接都必须发送一次相同的命令。
	 * 
	 * @param sourceMysql
	 * @param destMysqlConn
	 */
	//TODO 需要进行优化
	protected void appendPreMainCommand(){
		Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
		final MysqlConnection sourceMysql =(MysqlConnection) source;
		for(Connection destConn : connSet){
			MysqlConnection destMysqlConn = (MysqlConnection)destConn;
			if(!StringUtil.equalsIgnoreCase(sourceMysql.getSchema(), destMysqlConn.getSchema())){
				if(sourceMysql.getSchema() != null){
					QueryCommandPacket selectDBCommand = new QueryCommandPacket();
					selectDBCommand.query = sourceMysql.getSchema();
					selectDBCommand.command = QueryCommandPacket.COM_INIT_DB;
					
					byte[] buffer = selectDBCommand.toByteBuffer(destMysqlConn).array();
					CommandInfo info = new CommandInfo();
					info.setBuffer(buffer);
					info.setMain(false);
					info.setRunnable(new Runnable(){
						public void run() {
							Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
							for(Connection conn : connSet){
								((MysqlConnection)conn).setSchema(sourceMysql.getSchema());
							}
						}
					});
					commandQueue.appendCommand(info,true);
				}
			}
			
			if(sourceMysql.getCharset()!= null &&
					!StringUtil.equalsIgnoreCase(sourceMysql.getCharset(),destMysqlConn.getCharset())){
				QueryCommandPacket charsetCommand = new QueryCommandPacket();
				charsetCommand.query = "set names " + sourceMysql.getCharset();
				charsetCommand.command = QueryCommandPacket.COM_QUERY;
				
				byte[] buffer = charsetCommand.toByteBuffer(sourceMysql).array();
				CommandInfo info = new CommandInfo();
				info.setBuffer(buffer);
				info.setMain(false);
				info.setRunnable(new Runnable(){
					public void run() {
						Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
						for(Connection conn : connSet){
							((MysqlConnection)conn).setCharset(sourceMysql.getCharset());
						}
					}
				});
				commandQueue.appendCommand(info,true);
			}
			
			if(sourceMysql.isAutoCommit() != destMysqlConn.isAutoCommit()){
				QueryCommandPacket charsetCommand = new QueryCommandPacket();
				charsetCommand.query = "set autocommit = " + (sourceMysql.isAutoCommit()?1:0);
				charsetCommand.command = QueryCommandPacket.COM_QUERY;
				
				byte[] buffer = charsetCommand.toByteBuffer(sourceMysql).array();
				CommandInfo info = new CommandInfo();
				info.setBuffer(buffer);
				info.setMain(false);
				info.setRunnable(new Runnable(){
					public void run() {
						Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
						for(Connection conn : connSet){
							((MysqlConnection)conn).setAutoCommit(sourceMysql.isAutoCommit());
						}
					}
				});
				commandQueue.appendCommand(info,true);
			}
		}
	}
	
	/**
	 * this method will be invoked after main command response completed 
	 * @param conn
	 */
	protected void afterCommand(MysqlServerConnection conn,CommandStatus commStatus){
		
	}
	
	public synchronized void handleMessage(Connection fromConn) {
		byte[] message = null;
		lastTimeMillis = System.currentTimeMillis();
		if(fromConn == source){
			while((message = fromConn.getInQueue().getNonBlocking()) != null){
				CommandInfo info = new CommandInfo();
				info.setBuffer(message);
				info.setMain(true);
				
				if(!commandQueue.appendCommand(info,false)){
					dispatchMessageFrom(source,message);
				}
				logger.error("handle message from client after session started,handler="+this+", packet=\n"+StringUtil.dumpAsHex(message, message.length));
			}
			
		}else{
			while((message = fromConn.getInQueue().getNonBlocking()) != null){
				//判断命令是否完成了
				CommandStatus commStatus = commandQueue.checkResponseCompleted(fromConn, message);
				
				if(CommandStatus.AllCompleted == commStatus || CommandStatus.ConnectionCompleted == commStatus){
					
					//记录 prepared statement ID 或者 close statement
					afterCommand((MysqlServerConnection)fromConn,commStatus);
					
					if(commandQueue.currentCommand.isMain() || this.ended){
						//mysqlServer connection return to pool
						releaseConnection(fromConn);
					}
					if(this.ended){
						return;
					}
				}
				
				if(CommandStatus.AllCompleted == commStatus){
					ConnectionStatuts fromConnStatus = commandQueue.connStatusMap.get(fromConn);
					try{
						
						/**
						 * 如果是客户端请求的命令则:
						 * 1、请求是多台server的，需要进行合并数据
						 * 2、单台server直接写出到客户端
						 */
						
						if(commandQueue.currentCommand.isMain()){
							commandQueue.mainCommandExecuted = true;
							if(commandQueue.isMultiple()){
								if(fromConnStatus.isMerged){
									List<byte[]> list = this.mergeMessages();
									if(list != null){
										for(byte[] buffer : list){
											dispatchMessageFrom(fromConn,buffer);
										}
									}
								}
							}else{
								dispatchMessageFrom(fromConn,message);
							}
						}else{
							//非主命令发送以后返回出错信息，则结束当前的session
							Collection<ConnectionStatuts> connectionStatutsSet = commandQueue.connStatusMap.values();
							for(ConnectionStatuts connStatus : connectionStatutsSet){
								//看是否每个服务器返回的数据包都没有异常信息。
								if(connStatus.errorPacket != null){
									this.commandQueue.currentCommand.setStatusCode(connStatus.statusCode);
									if(!commandQueue.mainCommandExecuted){
										dispatchMessageFrom(connStatus.conn,connStatus.errorPacket.toByteBuffer(connStatus.conn).array());
										if(source.isAutoCommit()){
											this.endSession(false);
										}
									}else{
										if(logger.isDebugEnabled()){
											byte[] commandBuffer = commandQueue.currentCommand.getBuffer();
											StringBuffer buffer = new StringBuffer();
											buffer.append("Current Command Execute Error:\n");
											buffer.append(StringUtil.dumpAsHex(commandBuffer,commandBuffer.length));
											buffer.append("\n error Packet:\n");
											buffer.append(connStatus.errorPacket.toString());
											logger.debug(buffer.toString());
										}
									}
									return;
								}
							}
						}
					}finally{
						if(fromConnStatus.isMerged){
							afterCommandCompleted(commandQueue.currentCommand);
						}
					}
				}else{
					if(commandQueue.currentCommand.isMain()){
						if(!commandQueue.isMultiple()){
							dispatchMessageFrom(fromConn,message);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 当一个命令结束的时候，清理缓存的数据包。并且尝试发送下一个command
	 * 如果队列中没有命令，则结束当前回话
	 * @param oldCommand 当前的command
	 */
	protected synchronized void afterCommandCompleted(CommandInfo oldCommand){
		if(this.commandQueue.currentCommand != oldCommand){
			return;
		}
		if(oldCommand.getRunnable()!= null){
			oldCommand.getRunnable().run();
		}
		commandQueue.clearAllBuffer();

		//当一个命令的最后一个数据包到达，则将当前的命令从队列中删除。
		commandQueue.sessionInitQueryQueue.remove(0);
		if(!ended){
			startNextCommand();
		}
	}
	
	//判断是否需要继续发送下一条客户端命令
	//发送下一条命令
	protected synchronized void startNextCommand(){
		if(commandQueue.currentCommand != null && (commandQueue.currentCommand.getStatusCode() & SessionStatus.ERROR) >0){
			if(source.isAutoCommit()){
				this.endSession(false);
			}
			return;
		}
		
		if(!this.ended && commandQueue.tryNextCommandTuple()){
			commandType = commandQueue.currentCommand.getBuffer()[4];
			Collection<ConnectionStatuts> connSet = commandQueue.connStatusMap.values();
			
			boolean commandCompleted = commandQueue.currentCommand.getCompletedCount().get() == commandQueue.connStatusMap.size();
			
			boolean isProcedure = false;
			if(statment instanceof DMLStatement){
				DMLStatement dmlStatement = (DMLStatement)statment;
				isProcedure = dmlStatement.isProcedure();
			}
			
			for(ConnectionStatuts status : connSet){
				if(commandQueue.currentCommand.isMain() && isProcedure){
					status.setCommandType(commandType,true);
				}else{
					status.setCommandType(commandType,false);
				}
				
			}
			
			dispatchMessageFrom(source,commandQueue.currentCommand.getBuffer());
			
			if(commandCompleted){
				afterCommandCompleted(commandQueue.currentCommand);
			}
		}else{
			if(source.isAutoCommit()){
				this.endSession(false);
			}
		}
	}
	
	/**
	 * <pre>
	 * 任何在handler里面需要发送到目标连接的数据包，都调用该方法发送出去。
	 * 从服务器端发送过来的消息到客户端，或者从客户端发送命令到各个mysql server。
	 * 
	 * 这儿主要发送的消息有2种：
	 * 1、从客户端发送过来的消息
	 * 2、reponse当前的主要命令（是客户端发出来的命令而不是该proxy内部产生的命令）的数据包
	 * 以上2种数据包通过dispatchMessage 方法发送出去的。
	 * 由内部产生的命令数据包可以在 afterCommandCompleted()之后 根据ConnectionStatus.buffers中保存。
	 * commandQueue.clearAllBuffer() 以后buffers 将被清空
	 * </pre>
	 * @param fromServer 是否是从mysql server 端发送过来的
	 * @param message 消息内容
	 */
	protected void dispatchMessageFrom(Connection fromConn,byte[] message){
		if(fromConn != source){
			dispatchMessageTo(source,message);
		}else{
			Collection<MysqlServerConnection> connSet =  commandQueue.connStatusMap.keySet();
			for(Connection conn : connSet){
				dispatchMessageTo(conn,message);
			}
		}
	}
	
	/**
	 * 这儿将启动一些缓存机制，避免小数据包频繁调用 系统write, CommandMessageHandler类或者其子类必须通过该方法发送数据包
	 * @param toConn
	 * @param message
	 */
	protected void dispatchMessageTo(Connection toConn,byte[] message){
		
		if(toConn == source){
			if(message != null){
				appendBufferToWrite(message,buffer,toConn,false);
			}else{
				appendBufferToWrite(message,buffer,toConn,true);
			}
		}else{
			toConn.postMessage(message);
		}
		
	}
	
	/**
	 * 缓冲写数据到目的地
	 * @param byts
	 * @param buffer
	 * @param conn
	 * @param writeNow
	 * @return
	 */
	private synchronized boolean appendBufferToWrite(byte[] byts,PacketBuffer buffer,Connection conn,boolean writeNow){
		if(byts == null){
			if(buffer.getPosition()>0){
				conn.postMessage(buffer.toByteBuffer());
				buffer.reset();
			}
			return true;
		}else{
			if(writeNow || buffer.remaining() < byts.length){
				if(buffer.getPosition()>0){
					buffer.writeBytes(byts);
					conn.postMessage(buffer.toByteBuffer());
					buffer.reset();
				}else{
					conn.postMessage(byts);
				}
				return true;
			}else{
				buffer.writeBytes(byts);
				return true;
			}
		}
	}
	
	protected synchronized void releaseConnection(Connection conn){
		MessageHandler handler = handlerMap.remove(conn);
		if(handler != null){
			conn.setMessageHandler(handler);
		}
		
		if(conn instanceof MysqlServerConnection){
			PoolableObject pooledObject = (PoolableObject)conn;
			if(pooledObject.getObjectPool() != null && pooledObject.isActive()){
				try {
					pooledObject.getObjectPool().returnObject(conn);
					if(logger.isDebugEnabled()){
						logger.debug("connection:"+conn+" return to pool");
					}
				} catch (Exception e) {
				}
			}
		}
		
	}
	
	/**
	 * 关闭该messageHandler 并且恢复所有这个messageHandler所handle的Connection
	 */
	protected void releaseAllCompletedConnection(){
		Set<Map.Entry<Connection,MessageHandler>> handlerSet = handlerMap.entrySet();
		for(Map.Entry<Connection,MessageHandler> entry:handlerSet){
			MessageHandler handler = entry.getValue();
			Connection connection = entry.getKey();
			ConnectionStatuts status = this.commandQueue.connStatusMap.get(connection);
			if(this.commandQueue.currentCommand == null || !isStarted() || (status != null && (status.statusCode & SessionStatus.COMPLETED)>0)){
				connection.setMessageHandler(handler);
				if(!connection.isClosed()){
					if(connection instanceof MysqlServerConnection){
						PoolableObject pooledObject = (PoolableObject)connection;
						if(pooledObject.getObjectPool() != null){
							try {
								pooledObject.getObjectPool().returnObject(connection);
								if(logger.isDebugEnabled()){
									logger.debug("connection:"+connection+" return to pool");
								}
							} catch (Exception e) {
							}
						}
					}
				}
			}else{
				
			}
		}
	}
	
	/**
	 * 合并多服务端的消息，发送到客户端
	 * 只有在多连接的情况下需要进行数据包聚合，聚合以后逐一将数据包通过 {@link #dispatchMessageFrom(Connection, byte[])}方法发送出去,
	 * 一对一的连接直接通过{@link #dispatchMessageFrom(Connection, byte[])} 方法 直接发送出去,而不需要merge。
	 * @return
	 */
	protected synchronized List<byte[]> mergeMessages(){
		if(this.commandQueue.currentCommand.isMerged()){
			return null;
		}
		this.commandQueue.currentCommand.setMerged(true);
		Collection<ConnectionStatuts> connectionStatutsSet = commandQueue.connStatusMap.values();
		
		/**
		 * 表示是否返回具有查询结果的请求
		 */
		boolean isSelectQuery = true;
		boolean isCall = false;
		
		List<byte[]> buffers = null;
		List<byte[]> returnList = new ArrayList<byte[]>();
		for(ConnectionStatuts connStatus : connectionStatutsSet){
			//看是否每个服务器返回的数据包都没有异常信息。
			if(connStatus.buffers.size() ==0){
				for(ConnectionStatuts connStatus1 : connectionStatutsSet){
					
					StringBuffer buffer = new StringBuffer();
					
					buffer.append("<---connection="+connStatus1.conn.getInetAddress()+"=="+connStatus1.conn.getInetAddress()+"------->\n");
					for(byte[] buf : connStatus1.buffers){
						buffer.append(StringUtil.dumpAsHex(buf,buf.length)+"\n");
						buffer.append("------------\n");
					}
					buffer.append("<----error Packet:"+connStatus1.conn.getInetAddress()+"------>\n");
					logger.error(buffer.toString());
				}
				continue;
			}
			
			buffers = connStatus.buffers;
			
			if((connStatus.statusCode & SessionStatus.ERROR) >0){
				return buffers;
			}
			
		}
		
		if(commandQueue.statment instanceof DMLStatement){
			DMLStatement dmlStatement = (DMLStatement)commandQueue.statment;
			if(this.commandQueue.currentCommand.isMain()){
				isCall = dmlStatement.isProcedure();
			}
		}
		
		//以下是判断是否是更新操作还是查询操作
		if(!isCall){
			isSelectQuery = MysqlPacketBuffer.isEofPacket(buffers.get(buffers.size()-1));
		}else{
			isSelectQuery = !MysqlPacketBuffer.isOkPacket(buffers.get(0));
		}
		
		if(isSelectQuery){
			//当前的packetId
			byte paketId = 0;
			
			//发送field信息
			for(byte[] buffer : buffers){
				if(MysqlPacketBuffer.isEofPacket(buffer)){
					returnList.add(buffer);
					paketId = buffer[3];
					break;
				}else{
					returnList.add(buffer);
					paketId = buffer[3];
				}
			}
			paketId += 1;
			//发送rows数据包
			for(ConnectionStatuts connStatus : connectionStatutsSet){
				boolean rowStart = false;
				boolean isRowEnd = false;
				for(byte[] buffer : connStatus.buffers){
					if(!rowStart){
						if(MysqlPacketBuffer.isEofPacket(buffer)){
							rowStart = true;
						}else{
							continue;
						}
					}else{
						if(!MysqlPacketBuffer.isEofPacket(buffer)){
							if(!isRowEnd){
								buffer[3] = paketId;
								paketId += 1;
								returnList.add(buffer);
							}
						}else{
							isRowEnd = true;
						}
					}
				}
			}
			
			if(!isCall){
				byte[] eofBuffer = buffers.get(buffers.size()-1);
				eofBuffer[3] = paketId;
				returnList.add(eofBuffer);
			}else{
				byte[] eofBuffer = buffers.get(buffers.size()-2);
				eofBuffer[3] = paketId;
				returnList.add(eofBuffer);
				paketId ++;
				byte[] okBuffer = buffers.get(buffers.size()-1);
				okBuffer[3] = paketId;
				returnList.add(okBuffer);
			}
		}else{
			OkPacket ok = new OkPacket();
			StringBuffer strbuffer = new StringBuffer();
			for(ConnectionStatuts connStatus : connectionStatutsSet){
				byte[] buffer = connStatus.buffers.get(connStatus.buffers.size()-1);
				OkPacket connOK = new OkPacket();
				connOK.init(buffer,connStatus.conn);
				ok.affectedRows +=connOK.affectedRows;
				ok.insertId =connOK.insertId;
				ok.packetId = 1;
				strbuffer.append(connOK.message);
				ok.warningCount +=connOK.warningCount;
			}
			ok.message = strbuffer.toString();
			returnList.add(ok.toByteBuffer(source).array());
		}
		return returnList;
	}

	protected abstract ConnectionStatuts newConnectionStatuts(Connection conn);

	public boolean isStarted(){
		return this.started;
	}
	
	public synchronized void startSession() throws Exception {
		if(logger.isInfoEnabled()){
			logger.info("session start[type="+this.command.command+"]:ip="+this.source.getSocketId()+",handlerId="+this.hashCode()
					+",time="+(System.currentTimeMillis()-createTime)
					+",sql="+(this.statment ==null?null:this.statment.getSql()));
		}
		
		for(ObjectPool pool:pools){
			MysqlServerConnection conn;
			conn = (MysqlServerConnection)pool.borrowObject();
			handlerMap.put(conn, conn.getMessageHandler());
			if(conn.getMessageHandler() instanceof CommandMessageHandler){
				logger.error("current handler="+conn.getMessageHandler().toString()+",");
			}
			conn.setMessageHandler(this);
			commandQueue.connStatusMap.put(conn, newConnectionStatuts(conn));
		}
		
		this.started = true;
		appendPreMainCommand();
		this.commandQueue.appendCommand(info, true);
		startNextCommand();
	}
	
	public boolean checkIdle(long now) {
		if(timeout >0){
			return (now - createTime)>timeout;
		}else{
			if(ended){
				/**
				 * 如果该session已经结束，此时如果serverConnection端还在等待所有数据访问。并且超过15s,则需要当空闲的会话
				 * 避免由于各种原因造成服务器端没有发送数据或者已经结束的会话而ServerConnection无法返回Pool中。
				 */
				return (now - endTime)>15000;
			}else{
				return (now - lastTimeMillis) > ProxyRuntimeContext.getInstance().getRuntimeContext().getQueryTimeout() * 1000;
			}
		}
	}

	public  void endSession(boolean force) {
		if(!isEnded()){
			synchronized (this) {
				if(!ended){
					forceEnded = force;
					endTime = System.currentTimeMillis();
					ended = true;
				}else{
					return;
				}
			}
		}
		
		this.releaseAllCompletedConnection();
		if(!this.commandQueue.mainCommandExecuted){
			StringBuffer buffer = new StringBuffer();
			buffer.append("<<---client connection="+source.getSocketId()+",source handler ischanged="+(source.getMessageHandler()==this)+",\n session Handler="+this+"----->>\n");
			for(Map.Entry<MysqlServerConnection, ConnectionStatuts> entry : commandQueue.connStatusMap.entrySet()){
				if((entry.getValue().statusCode & SessionStatus.COMPLETED) == 0){
					buffer.append("<----start-connection="+entry.getKey().getSocketId()
							+",queueSize="+entry.getKey().getInQueueSize()
							+",manager="+entry.getKey().getConnectionManager().getName() 
							+",managerRunning="+entry.getKey().getConnectionManager().isRunning()
							+",selectorOpened="+entry.getKey().getConnectionManager().getSelector().isOpen()+"-------\n");
					for(byte[] buf : entry.getValue().buffers){
						buffer.append(StringUtil.dumpAsHex(buf,buf.length)+"\n");
						buffer.append("\n");
					}
					buffer.append("<----end connection:"+entry.getKey().getSocketId()+"------>\n");
				}else{
					buffer.append("<----start -- end Packet:"+entry.getKey().getSocketId()+",COMPLETED = true------>\n");	
				}
				
				if(force){
					entry.getKey().postClose(null);
				}
				logger.error(buffer.toString());
			}

			if(this.errorPacket == null){
				errorPacket = new ErrorPacket();
				errorPacket.errno = 10000;
				errorPacket.packetId = 2;
				errorPacket.serverErrorMessage = " session was killed!!";
				this.dispatchMessageTo(source, errorPacket.toByteBuffer(source).array());
				logger.warn("session was killed!!",new Exception());
			}
			
			source.postClose(null);
		}else{
			if(logger.isInfoEnabled()){
				logger.info("session end[type="+this.command.command+"]:ip="+this.source.getSocketId()
						+",handlerId="+this.hashCode()
						+",sql="+(this.statment ==null?null:this.statment.getSql()));
			}
		}
		this.dispatchMessageTo(source,null);
	}
	

	public synchronized boolean isEnded() {
		return this.ended;
	}
	
	public void appendReport(StringBuilder buffer, long now, long sinceLast,boolean reset,Level level) {
		buffer.append("    -- MessageHandler:").append("multiple Size:").append(commandQueue.connStatusMap.size());
		if(commandQueue.currentCommand != null){
			buffer.append(",currentCommand completedCount:");
			buffer.append(commandQueue.currentCommand.getCompletedCount()).append("\n");
		}else{
			buffer.append("\n");
		}
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("class=").append(this.getClass().getName());
		buffer.append(",createTime=").append(createTime);
		buffer.append(",endTime=").append(this.endTime);
		buffer.append(",lastTimeMillis=").append(this.lastTimeMillis);
		buffer.append(",ended=").append(this.ended );
		buffer.append(",forceEnded=").append(this.forceEnded );
		buffer.append(",started=").append(this.started );
		buffer.append(",ServerConnectionSize=").append(this.handlerMap.size());
		if(commandQueue.currentCommand != null){
			buffer.append(",currentCommand[").append("CompletedCount=").append(this.commandQueue.currentCommand != null ?this.commandQueue.currentCommand.getCompletedCount().get():"");
			buffer.append(",buffer=\n").append(StringUtil.dumpAsHex(commandQueue.currentCommand.getBuffer(),commandQueue.currentCommand.getBuffer().length));
		}
		buffer.append(", sql=").append(statment!= null?statment.getSql():"null");
		return buffer.toString();
	}

}
