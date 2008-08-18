package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * 
 * @author struct
 *
 */
public class T4C8TTIdtyResponseDataPacket extends T4CTTIMsgPacket {
	
	public boolean typeValid;
	public T4C8TTIdtyResponseDataPacket(){
		this.msgCode = TTIDTY;
	}
	
	protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
        typeValid = validTypeReps(meg); 
	}
	
	boolean validTypeReps(T4CPacketBuffer meg){
        boolean flag = false;
        int i = 0;
        do {
            byte byte0;
            do {
                byte0 = (byte) meg.unmarshalUB1();
                if (flag)
                    break;
                if (byte0 == 0)
                    return true;
                flag = true;
            } while (true);
            switch (i) {
                case 0: // '\0'
                    if (byte0 == 0)
                        flag = false;
                    else
                        i = 1;
                    break;
                case 1: // '\001'
                    i = 0;
                    break;
            }
        } while (true);
    }
	
	protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
		meg.marshalUB1((byte)0);
	}
}
