package com.meidusa.amoeba.aladdin.io;

import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.OKforPreparedStatementPacket;
import com.meidusa.amoeba.net.Connection;

public class PreparedResultPacket extends ErrorResultPacket{
	private int parameterCount;
	private long statementId;
	
	public long getStatementId() {
		return statementId;
	}

	public void setStatementId(long statementId) {
		this.statementId = statementId;
	}

	public void setParameterCount(int count){
		this.parameterCount = count;
	}

	public int getParameterCount() {
		return parameterCount;
	}
	
	public void wirteToConnection(Connection conn) {
		if(this.isError()){
			super.wirteToConnection(conn);
		}else{
			OKforPreparedStatementPacket okPaket = new OKforPreparedStatementPacket();
			okPaket.columns = 0;
			okPaket.packetId = 1;
			okPaket.parameters = parameterCount;
			okPaket.statementHandlerId = statementId;
			conn.postMessage(okPaket.toByteBuffer(conn));
			for(int i=0;i<parameterCount;i++){
				FieldPacket field = new  FieldPacket();
				conn.postMessage(field.toByteBuffer(conn));
			}
		}
	}
}
