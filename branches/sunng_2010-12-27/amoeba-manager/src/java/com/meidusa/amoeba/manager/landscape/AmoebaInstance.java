package com.meidusa.amoeba.manager.landscape;

import com.meidusa.amoeba.manager.landscape.lifecycle.LifeCycleState;

import java.util.Date;

/**
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:32 PM
 */
public class AmoebaInstance implements AmoebaInstanceType {

    private LifeCycleState state;

    private String InstanceId;

    public LifeCycleState getState() {
        return state;
    }

    public void setState(LifeCycleState state) {
        this.state = state;
    }

    public String getInstanceId() {
        return InstanceId;
    }

    public void setInstanceId(String instanceId) {
        InstanceId = instanceId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    private Date createdDate;

}
