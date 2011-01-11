package com.meidusa.amoeba.manager.landscape.operation;

import com.meidusa.amoeba.manager.landscape.AmoebaInstance;
import com.meidusa.amoeba.manager.landscape.lifecycle.LifeCycleState;

import java.util.List;

/**
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 10:06 PM
 */
public interface Operation {

    public List<LifeCycleState> availableForStatues();

    public void perform();

    public boolean check();

    public void operateOn(List<AmoebaInstance> instances);

}
