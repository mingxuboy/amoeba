package com.meidusa.amoeba.manager.landscape.lifecycle;

/**
 * A provisioning server is ready
 * <p/>
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:15 PM
 */
public class LifeCycleStatePlanned implements LifeCycleState {

    private String hostName;

    private int sshPort;

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public String getStatusName() {
        return "PLANNED";
    }
}
