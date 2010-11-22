package com.meidusa.amoeba.mysql.handler;

import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.net.Connection;

public class PreparedStatmentExecuteConnectionStatuts extends PreparedStatmentConnectionStatuts{
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