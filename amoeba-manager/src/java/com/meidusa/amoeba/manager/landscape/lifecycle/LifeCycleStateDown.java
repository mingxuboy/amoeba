package com.meidusa.amoeba.manager.landscape.lifecycle;

import java.util.Date;

/**
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:18 PM
 */
public class LifeCycleStateDown implements LifeCycleState {

    private Date downTime;

    @Override
    public String getStatusName() {
        return "DOWN";
    }
}
