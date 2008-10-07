package com.meidusa.amoeba.aladdin.io;

import java.util.ArrayList;
import java.util.List;

import com.meidusa.amoeba.mysql.net.packet.EOFPacket;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;
import com.meidusa.amoeba.net.Connection;

/**
 * 
 * @author struct
 *
 */
public class MysqlResultSetPacket extends ErrorResultPacket {
	
	public ResultSetHeaderPacket resulthead;
	public FieldPacket[] fieldPackets;
	public List<RowDataPacket> rowList = new ArrayList<RowDataPacket>();
	
	public void addRowDataPacket(RowDataPacket row){
		synchronized (rowList) {
			rowList.add(row);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.meidusa.amoeba.aladdin.io.ResultPacket#wirteToConnection(com.meidusa.amoeba.net.Connection)
	 */
	public void wirteToConnection(Connection conn){
		if(isError()){
			super.wirteToConnection(conn);
			return;
		}
		//write header bytes
		byte paketId = resulthead.packetId;
		conn.postMessage(resulthead.toByteBuffer(conn));
		
		//write fields bytes
		for(int i=0;i<fieldPackets.length;i++){
			conn.postMessage(fieldPackets[i].toByteBuffer(conn));
			paketId = fieldPackets[i].packetId;
		}
		
		//write eof bytes
		EOFPacket eof = new EOFPacket();
		eof.serverStatus = 2;
		eof.warningCount = 0;
		eof.packetId = (++paketId);
		conn.postMessage(eof.toByteBuffer(conn));
		
		//write rows bytes
		for(RowDataPacket row : rowList){
			row.packetId = (++paketId);
			conn.postMessage(row.toByteBuffer(conn));
		}

		//write eof bytes
		eof.packetId = (++paketId);
		conn.postMessage(eof.toByteBuffer(conn));
	}
}
