package com.meidusa.amoeba.manager.net.packet;

public class OKPacket extends ManagerAbstractPacket {

    public OKPacket(){
        this.funType = ManagerAbstractPacket.FUN_TYPE_OK;
    }
}
