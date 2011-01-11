package com.meidusa.amoeba.manager.net.packet;

import java.io.UnsupportedEncodingException;

/**
 * @author struct
 */
public class ObjectPacket extends ManagerAbstractPacket {

    public Object object;

    protected void init(ManagerPacketBuffer buffer) {
        super.init(buffer);
        ManagerPacketBuffer mBuffer = (ManagerPacketBuffer) buffer;
        if(mBuffer.hasRemaining()){
        	object = mBuffer.readObject();
        }
    }

    @Override
    protected void write2Buffer(ManagerPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        ManagerPacketBuffer mBuffer = (ManagerPacketBuffer) buffer;
        if(object != null){
        	mBuffer.writeObject(object);
        }
    }
}
