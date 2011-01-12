package com.meidusa.amoeba.manager.landscape;

/**
 * User: Sun Ning<classicning@gmail.com>
 * Date: 1/12/11
 * Time: 10:01 PM
 */
public class StorageInstance {

    private AmoebaInstanceType type;

    private String instanceId;

    public AmoebaInstanceType getType() {
        return type;
    }

    public void setType(AmoebaInstanceType type) {
        this.type = type;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
