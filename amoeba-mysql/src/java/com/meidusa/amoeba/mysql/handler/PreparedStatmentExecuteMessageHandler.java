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

import java.util.List;

import com.meidusa.amoeba.mysql.handler.session.ConnectionStatuts;
import com.meidusa.amoeba.mysql.net.CommandInfo;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.MysqlServerConnection;
import com.meidusa.amoeba.mysql.net.packet.CommandPacket;
import com.meidusa.amoeba.mysql.net.packet.ExecutePacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.PreparedStatmentClosePacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.statement.Statement;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class PreparedStatmentExecuteMessageHandler extends PreparedStatmentMessageHandler{
	
	static class PreparedStatmentExecuteConnectionStatuts extends PreparedStatmentMessageHandler.PreparedStatmentConnectionStatuts{
		public PreparedStatmentExecuteConnectionStatuts(Connection conn,PreparedStatmentInfo preparedStatmentInfo){
			super(conn,preparedStatmentInfo);
		}
		
		@Override
		public boolean isCompleted(byte[] buffer) {
			if(this.commandType == QueryCommandPacket.COM_STMT_EXECUTE){
				if(MysqlPacketBuffer.isEofPacket(buffer)){
					if((this.statusCode & PreparedStatmentSessionStatus.EOF_FIELDS)==0){
						this.statusCode |= PreparedStatmentSessionStatus.EOF_FIELDS;
						return false;
					}else{
						this.statusCode |= PreparedStatmentSessionStatus.EOF_ROWS;
						this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
						return true;
					}
				}else if(packetIndex == 0 && MysqlPacketBuffer.isErrorPacket(buffer)){
					this.statusCode |= PreparedStatmentSessionStatus.ERROR;
					this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
					this.setErrorPacket(buffer);
					return true;
				}else if(packetIndex == 0 && MysqlPacketBuffer.isOkPacket(buffer)){
					this.statusCode |= PreparedStatmentSessionStatus.OK;
					this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
					return true;
				}
				return false;
			}else if(this.commandType == QueryCommandPacket.COM_STMT_SEND_LONG_DATA){
				return true;
			}else{
				return super.isCompleted(buffer);
			}
		}
	}
	
	/** 当前的请求数据包 */
	
	public PreparedStatmentExecuteMessageHandler(MysqlClientConnection source,PreparedStatmentInfo preparedStatmentInfo,Statement statment,byte[] query,ObjectPool[] pools,long timeout){
		super(source,preparedStatmentInfo,statment,query,pools,timeout,true);
	}

	private ExecutePacket executePacket;
	
	public ExecutePacket getExecutePacket() {
		return executePacket;
	}
	public void setExecutePacket(ExecutePacket executePacket) {
		this.executePacket = executePacket;
	}
	protected void appendPreMainCommand(){
		super.appendPreMainCommand();
		QueryCommandPacket preparedCommandPacket = new QueryCommandPacket();
		preparedCommandPacket.command = CommandPacket.COM_STMT_PREPARE;
		preparedCommandPacket.query = preparedStatmentInfo.getPreparedStatment();
		byte[] buffer = preparedCommandPacket.toByteBuffer(source).array();
		CommandInfo info = new CommandInfo();
		info.setBuffer(buffer);
		info.setMain(false);
		commandQueue.appendCommand(info,true);
		for(byte[] longData:this.source.getLongDataList()){
			CommandInfo longDataCommand = new CommandInfo();
			longDataCommand.setBuffer(longData);
			longDataCommand.setMain(false);
			longDataCommand.getCompletedCount().set(this.commandQueue.connStatusMap.size());
			commandQueue.appendCommand(longDataCommand,true);
		}
		source.clearLongData();
	}
	protected  void afterMainCommand(MysqlServerConnection conn){
		super.afterMainCommand(conn);
		if (commandType == QueryCommandPacket.COM_STMT_EXECUTE) {
			PreparedStatmentClosePacket preparedCloseCommandPacket = new PreparedStatmentClosePacket();
	        preparedCloseCommandPacket.command = CommandPacket.COM_STMT_CLOSE;
	        preparedCloseCommandPacket.statementId = statmentIdMap.get(conn);
	        conn.postMessage(preparedCloseCommandPacket.toByteBuffer(conn));
	        if(logger.isInfoEnabled()){
	        	logger.info("conn="+conn.getSocketId()+", close statement id="+preparedCloseCommandPacket.statementId);
	        }
		}
	}
	
	protected void afterCommandCompleted(CommandInfo currentCommand){
		super.afterCommandCompleted(currentCommand);
	}

	@Override
	protected ConnectionStatuts newConnectionStatuts(Connection conn) {
		return new PreparedStatmentExecuteConnectionStatuts(conn,this.preparedStatmentInfo);
	}

	protected List<byte[]> mergeMessages() {
		return super.mergeMessages();
	}
	public String toString(){
		String parameter = "";
		if(executePacket.getParameters() != null){
			StringBuffer buffer = new StringBuffer();
			for(Object object :executePacket.getParameters()){
				buffer.append(object).append(",");
			}
			if(buffer.length() > 0){
				parameter = buffer.substring(0, buffer.length()-1);
			}
		}
		return super.toString() +" ,parameter=["+parameter+"]";
	}
}
