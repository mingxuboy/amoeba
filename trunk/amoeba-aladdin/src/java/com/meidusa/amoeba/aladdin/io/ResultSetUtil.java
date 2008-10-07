package com.meidusa.amoeba.aladdin.io;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;

/**
 * 
 * @author struct
 *
 */
public class ResultSetUtil {
	private static Logger logger = Logger.getLogger(ResultSetUtil.class);
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
					for(int i=0;i<colunmCount;i++){
						int j=i+1;
						packet.fieldPackets[i] = new FieldPacket();
						packet.fieldPackets[i].orgName = metaData.getColumnName(j);
						packet.fieldPackets[i].name = metaData.getColumnLabel(j);
						packet.fieldPackets[i].catalog = "def".intern();
						packet.fieldPackets[i].type = (byte)MysqlDefs.javaTypeMysql(metaData.getColumnType(j));
					}
				}
			}
		}
		
		while(rs.next()){
			RowDataPacket row = new RowDataPacket();
			row.columns = new ArrayList<String>(colunmCount);
			for(int i=0;i<colunmCount;i++){
				int j=i+1;
				row.columns.add(rs.getString(j));
			}
			if(logger.isDebugEnabled()){
				logger.debug("fetch result row:"+row);
			}
			packet.addRowDataPacket(row);
		}
	}
	
	public static void metaDataToPacket(MysqlResultSetPacket packet,ResultSetMetaData metaData) throws SQLException{
			
		int colunmCount = metaData.getColumnCount();
		synchronized (packet) {
			if(packet.resulthead == null){
				packet.resulthead = new ResultSetHeaderPacket();
				packet.resulthead.columns = colunmCount;
			}
			
			if(colunmCount>0){
				if(packet.fieldPackets == null){
					packet.fieldPackets = new FieldPacket[colunmCount];
					for(int i=0;i<colunmCount;i++){
						int j=i+1;
						packet.fieldPackets[i] = new FieldPacket();
						packet.fieldPackets[i].orgName = metaData.getColumnName(j);
						packet.fieldPackets[i].name = metaData.getColumnLabel(j);
						packet.fieldPackets[i].catalog = "def".intern();
						packet.fieldPackets[i].type = (byte)MysqlDefs.javaTypeMysql(metaData.getColumnType(j));
					}
				}
			}
		}
	}
}
