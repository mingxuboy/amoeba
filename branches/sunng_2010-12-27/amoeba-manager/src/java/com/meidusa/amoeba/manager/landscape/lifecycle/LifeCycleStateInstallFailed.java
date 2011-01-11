package com.meidusa.amoeba.manager.landscape.lifecycle;

/**
 * User: Sun Ning <Classicning@gmail.com>
 *
 * Date: 1/11/11
 * Time: 8:52 PM
 */
public class LifeCycleStateInstallFailed implements LifeCycleState {

    @Override
    public String getStatusName() {
        return "INSTALL_FAILED";
    }
}
