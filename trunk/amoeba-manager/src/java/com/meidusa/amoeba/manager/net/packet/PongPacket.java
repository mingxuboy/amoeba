package com.meidusa.amoeba.manager.net.packet;

public class PongPacket extends ManagerAbstractPacket {

    public PongPacket(){
        this.funType = ManagerAbstractPacket.FUN_TYPE_PONG;
    }

}
