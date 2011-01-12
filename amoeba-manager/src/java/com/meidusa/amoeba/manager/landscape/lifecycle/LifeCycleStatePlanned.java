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

    private String username;

    private String password;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getStatusName() {
        return "PLANNED";
    }
}
