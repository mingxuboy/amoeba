package com.meidusa.amoeba.manager.landscape.lifecycle;

/**
 * Amoeba software is installed on this server. All tests passed.
 *
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:15 PM
 */
public class LifeCycleStateInstalled implements LifeCycleState{

    @Override
    public String getStatusName() {
        return "INSTALLED";
    }
}
