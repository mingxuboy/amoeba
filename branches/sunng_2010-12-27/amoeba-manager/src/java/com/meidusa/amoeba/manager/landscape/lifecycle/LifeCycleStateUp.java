package com.meidusa.amoeba.manager.landscape.lifecycle;

import java.util.Date;

/**
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:16 PM
 */
public class LifeCycleStateUp implements LifeCycleState {

    private Date upTime;

    private int controlPort;

    private int workingPort;

    private String hostName;

    private int pid;

    public Date getUpTime() {
        return upTime;
    }

    public void setUpTime(Date upTime) {
        this.upTime = upTime;
    }

    public int getControlPort() {
        return controlPort;
    }

    public void setControlPort(int controlPort) {
        this.controlPort = controlPort;
    }

    public int getWorkingPort() {
        return workingPort;
    }

    public void setWorkingPort(int workingPort) {
        this.workingPort = workingPort;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public String getStatusName() {
        return "UP";
    }
}
