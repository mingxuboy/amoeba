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

import java.util.ArrayList;
import java.util.List;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.net.packet.EOFPacket;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.OKforPreparedStatementPacket;
import com.meidusa.amoeba.net.DatabaseConnection;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class PreparedStatmentInfo{
	/**
	 * 客户端发送过来的 prepared statment sql语句
	 */
	private String preparedStatment;
	//private OKforPreparedStatementPacket okPrepared;
	private int parameterCount;
	/**
	 * 需要返回给客户端
	 */
	private List<byte[]> preparedStatmentPackets = new ArrayList<byte[]>();
	
	private long statmentId;
	
	public PreparedStatmentInfo(DatabaseConnection conn,long id,String preparedSql){
		statmentId = id;
		this.preparedStatment = preparedSql;
		OKforPreparedStatementPacket okPaket = new OKforPreparedStatementPacket();
		okPaket.columns = 0;
		okPaket.packetId = 1;
		byte packetId = 1;
		parameterCount = ProxyRuntimeContext.getInstance().getQueryRouter().parseParameterCount(conn, preparedSql);
		okPaket.parameters = parameterCount;
		okPaket.statementHandlerId = statmentId;
		preparedStatmentPackets.add(okPaket.toByteBuffer(conn).array());
		if(parameterCount>0){
			for(int i=0;i<parameterCount;i++){
				FieldPacket field = new  FieldPacket();
				field.packetId = (byte)(++packetId);
				
				preparedStatmentPackets.add(field.toByteBuffer(conn).array());
			}
			EOFPacket eof = new EOFPacket();
			eof.packetId = ++packetId;
			eof.serverStatus = 2;
			
			preparedStatmentPackets.add(eof.toByteBuffer(conn).array());
		}
		
		if(okPaket.columns>0){
			for(int i=0;i<okPaket.columns;i++){
				FieldPacket field = new  FieldPacket();
				field.packetId = (byte)(++packetId);
				preparedStatmentPackets.add(field.toByteBuffer(conn).array());
			}
			EOFPacket eof = new EOFPacket();
			eof.packetId = ++packetId;
			eof.serverStatus = 2;
			preparedStatmentPackets.add(eof.toByteBuffer(conn).array());
		}
	}
	
	public int getParameterCount() {
		return parameterCount;
	}

	/*public OKforPreparedStatementPacket getOkPrepared() {
		return okPrepared;
	}

	public void setOkPrepared(OKforPreparedStatementPacket okPrepared) {
		this.okPrepared = okPrepared;
	}*/
	
	/**
	 * @see {@link #buffersIsFull}
	 * @return
	 */
	public List<byte[]> getPreparedStatmentBuffers(){
		return preparedStatmentPackets;
	}

	public long getStatmentId() {
		return statmentId;
	}
	
	public String getPreparedStatment() {
		return preparedStatment;
	}

	public void setPreparedStatment(String preparedStatment) {
		this.preparedStatment = preparedStatment;
	}
}