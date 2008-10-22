package com.meidusa.amoeba.aladdin.handler;

import java.util.ArrayList;

import com.meidusa.amoeba.aladdin.io.MysqlResultSetPacket;
import com.meidusa.amoeba.aladdin.io.ResultPacket;
import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;
import com.meidusa.amoeba.net.MessageHandler;

/**
 * 这个负责mysql 特有的一些 select query 的应答
 * @author struct
 *
 */
public class MysqlMessageHandlerRunner implements MessageHandlerRunner {
	private ResultPacket packet = null;
	private String query;
	
	public MysqlMessageHandlerRunner(){
		
	}
	public void init(MessageHandler handler) {
		CommandMessageHandler chandler = (CommandMessageHandler)handler;
		this.packet = chandler.packet;
		this.query = chandler.query;
	}

	public void reset() {
		this.packet = null;
		this.query = null;
	}

	public void run() {
		query = query.toLowerCase();
		String select = query.substring(0,"select".length());
		boolean isSelect = select.indexOf("select") >=0 || select.indexOf("show")>=0;
		if(isSelect){
			if(query.indexOf("@@sql_mode")>0){
				MysqlResultSetPacket resultPacket = (MysqlResultSetPacket)packet;
				ResultSetHeaderPacket resultHeader = new ResultSetHeaderPacket();
				resultHeader.packetId = 1;
				resultHeader.columns =1;
				
				resultPacket.resulthead = resultHeader;
				FieldPacket field = new FieldPacket();
				field.name = "@@sql_mode";
				field.length = 8;
				field.type = (byte)MysqlDefs.FIELD_TYPE_VAR_STRING;
				
				resultPacket.fieldPackets = new FieldPacket[]{field};
				RowDataPacket row = new RowDataPacket();
				row.columns = new ArrayList<String>();
				row.columns.add("STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION");
				resultPacket.addRowDataPacket(row);
			}else{
				MysqlResultSetPacket resultPacket = (MysqlResultSetPacket)packet;
				ResultSetHeaderPacket resultHeader = new ResultSetHeaderPacket();
				resultHeader.packetId = 1;
				resultHeader.columns =1;
				
				resultPacket.resulthead = resultHeader;
				FieldPacket field = new FieldPacket();
				field.name = "test";
				field.type = (byte)MysqlDefs.FIELD_TYPE_VAR_STRING;
				field.length = 8;
				resultPacket.fieldPackets = new FieldPacket[]{field};
			}
		}else{
			
		}
	}

}
