package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.Connection;

public class RedirectPacket extends AbstractPacket implements SQLnetDef {

    public RedirectPacket(){
        super(NS_PACKT_TYPE_REDIRECT);
    }

    public void init(byte[] buffer, Connection conn) {
        super.init(buffer, conn);
        this.dataOffset = 10;
        dataLen = buffer[8] & 0xff;
        dataLen <<= 8;
        dataLen |= buffer[9] & 0xff;
        extractData();
    }

}
