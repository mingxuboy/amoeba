package com.meidusa.amoeba.manager.landscape;

/**
 * User: Sun Ning<classicning@gmail.com>
 * Date: 1/12/11
 * Time: 10:05 PM
 */
public class MySQLInstance extends StorageInstance {

    private String hostName;

    private int port;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
