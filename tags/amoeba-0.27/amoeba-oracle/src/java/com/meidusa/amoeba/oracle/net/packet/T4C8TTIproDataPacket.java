package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * 协议数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:29:53
 */
public class T4C8TTIproDataPacket extends T4CTTIMsgPacket {

    /**
     * 客户端所能解析的版本协议列表
     */
    byte[] proCliVerTTC8 = { 6, 5, 4, 3, 2, 1 };

    /**
     * 客户端版本信息
     */
    String proCliStrTTC8 = "Java_TTC-8.2.0";

    public T4C8TTIproDataPacket(){
        super(TTIPRO);
    }

    @Override
    protected void marshal(AbstractPacketBuffer buffer) {
        super.marshal(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.writeBytes(proCliVerTTC8);
        meg.marshalNULLPTR();
        meg.writeBytes(proCliStrTTC8.getBytes());
        meg.marshalNULLPTR();
    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        proCliVerTTC8 = meg.unmarshalTEXT(10);
        proCliStrTTC8 = new String(meg.unmarshalTEXT(50));
    }

}
