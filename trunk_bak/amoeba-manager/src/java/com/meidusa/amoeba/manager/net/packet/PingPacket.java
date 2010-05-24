package com.meidusa.amoeba.manager.net.packet;

public class PingPacket extends ManagerAbstractPacket {

    public PingPacket(){
        this.funType = ManagerAbstractPacket.FUN_TYPE_PING;
    }

}
