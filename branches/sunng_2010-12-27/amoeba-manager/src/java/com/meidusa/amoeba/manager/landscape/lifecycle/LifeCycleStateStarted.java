package com.meidusa.amoeba.manager.landscape.lifecycle;

import java.util.Date;

/**
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:54 PM
 */
public class LifeCycleStateStarted implements LifeCycleState {

    private String hostName;

    private Date startedTime;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Date getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(Date startedTime) {
        this.startedTime = startedTime;
    }

    @Override
    public String getStatusName() {
        return "STARTED";
    }
}
