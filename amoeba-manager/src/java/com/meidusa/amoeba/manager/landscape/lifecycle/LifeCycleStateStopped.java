package com.meidusa.amoeba.manager.landscape.lifecycle;

import java.util.Date;

/**
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:20 PM
 */
public class LifeCycleStateStopped implements LifeCycleState {

    private Date stoppedTime;

    private String reason;

    @Override
    public String getStatusName() {
        return "STOPPED";
    }
}
