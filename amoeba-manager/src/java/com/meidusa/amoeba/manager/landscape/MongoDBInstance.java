package com.meidusa.amoeba.manager.landscape;

/**
 * User: Sun Ning<classicning@gmail.com>
 * Date: 1/12/11
 * Time: 10:06 PM
 */
public class MongoDBInstance extends StorageInstance {

    private String hostName;

    private int port;

    private int softwareVersion;

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

    public int getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(int softwareVersion) {
        this.softwareVersion = softwareVersion;
    }
}
