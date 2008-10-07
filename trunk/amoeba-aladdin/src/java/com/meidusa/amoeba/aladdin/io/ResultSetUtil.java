package com.meidusa.amoeba.aladdin.io;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;

public class ResultSetUtil {
	
	public static void resultSetToPacket(MysqlResultSetPacket packet,ResultSet rs) throws SQLException{
		
		ResultSetMetaData metaData = rs.getMetaData();
		int colunmCount = metaData.getColumnCount();
		synchronized (packet) {
			if(packet.resulthead == null){
				packet.resulthead = new ResultSetHeaderPacket();
				packet.resulthead.columns = colunmCount;
			}
			
			if(colunmCount>0){
				if(packet.fieldPackets == null){
					packet.fieldPackets = new FieldPacket[colunmCount];
					for(int i=1;i<=colunmCount;i++){
						packet.fieldPackets[i] = new FieldPacket();
						packet.fieldPackets[i].orgName = metaData.getColumnName(i);
						packet.fieldPackets[i].name = metaData.getColumnLabel(i);
						packet.fieldPackets[i].catalog = "def".intern();
						packet.fieldPackets[i].type = (byte)MysqlDefs.javaTypeMysql(metaData.getColumnType(i));
					}
				}
			}
		}
		
		while(rs.next()){
			RowDataPacket row = new RowDataPacket();
			row.columns = new ArrayList<String>(colunmCount);
			for(int i=1;i<=colunmCount;i++){
				row.columns.add(rs.getString(colunmCount));
			}
			packet.addRowDataPacket(row);
		}
	}
}
