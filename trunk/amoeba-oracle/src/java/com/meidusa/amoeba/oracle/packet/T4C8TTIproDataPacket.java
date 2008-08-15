package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * 协议数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:29:53
 */
public class T4C8TTIproDataPacket extends DataPacket implements T4CTTIMsg {

    protected void init(AbstractPacketBuffer buffer) {
        // length = buffer.unmarshalUB2();
        // packetCheckSum = buffer.readUB2();
        // type = buffer.readUB1();
        // flags = buffer.readUB1();
        // headerCheckSum = buffer.readUB2();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        super.write2Buffer(buffer);
    }
    
    @Override
	protected Class<? extends AbstractPacketBuffer> getBufferClass() {
		return T4CPacketBuffer.class;
	}

}
