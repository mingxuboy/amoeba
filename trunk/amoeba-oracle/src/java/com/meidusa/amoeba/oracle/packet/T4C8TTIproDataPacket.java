package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

/**
 * 协议数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:29:53
 */
public class T4C8TTIproDataPacket extends DataPacket implements T4CTTIMsg {

    @Override
    public void init(byte[] buffer) {        
        init(new T4CPacketBuffer(buffer));
    }

    protected void init(T4CPacketBuffer buffer) {
        // length = buffer.unmarshalUB2();
        // packetCheckSum = buffer.readUB2();
        // type = buffer.readUB1();
        // flags = buffer.readUB1();
        // headerCheckSum = buffer.readUB2();
    }

    @Override
    protected void write2Buffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        super.write2Buffer(buffer);
    }

}
