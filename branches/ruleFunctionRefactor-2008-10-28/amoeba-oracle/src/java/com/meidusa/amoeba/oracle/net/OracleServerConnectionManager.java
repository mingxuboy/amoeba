package com.meidusa.amoeba.oracle.net;

import java.io.IOException;

import com.meidusa.amoeba.net.AuthingableConnectionManager;
import com.meidusa.amoeba.net.Connection;

/**
 * @author struct
 */
public class OracleServerConnectionManager extends AuthingableConnectionManager {

    public OracleServerConnectionManager() throws IOException{
        super();
    }

    public OracleServerConnectionManager(String managerName) throws IOException{
        super(managerName);
    }

    protected void beforeAuthing(Connection authing) {
        super.beforeAuthing(authing);
    }
}
