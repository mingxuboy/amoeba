package com.meidusa.amoeba.manager.landscape.lifecycle;

/**
 * A provisioning server is ready
 *
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:15 PM
 */
public class LifeCycleStatePlanned implements  LifeCycleState {


    @Override
    public String getStatusName() {
        return "PLANNED";
    }
}
