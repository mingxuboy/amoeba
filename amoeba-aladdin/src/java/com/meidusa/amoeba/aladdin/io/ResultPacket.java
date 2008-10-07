package com.meidusa.amoeba.aladdin.io;

import com.meidusa.amoeba.net.Connection;

public interface ResultPacket {
	
	public void setError(int errorCode,String errorMessage);
	/**
	 * 将ResultSet这些包合并以后写到Connection
	 * head--> fields --> eof -->rows --> eof
	 * @param conn
	 */
	public abstract void wirteToConnection(Connection conn);

}