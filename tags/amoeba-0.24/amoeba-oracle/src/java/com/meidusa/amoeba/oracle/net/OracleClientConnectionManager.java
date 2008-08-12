package com.meidusa.amoeba.oracle.net;

import java.io.IOException;

import com.meidusa.amoeba.net.ServerableConnectionManager;

public class OracleClientConnectionManager extends ServerableConnectionManager {

	public OracleClientConnectionManager() throws IOException {
		super();
	}
	
	public OracleClientConnectionManager(String managerName,int port) throws IOException {
		super(managerName,port);
	}
	
	public OracleClientConnectionManager(String name,String ipAddress,int port) throws IOException{
		super(name,ipAddress,port);
	}

}
