package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.util.T4CTypeRep;

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
        setBasicTypes(meg.getTypeRep());
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
	
	void setBasicTypes(T4CTypeRep t4ctyperep){
        t4ctyperep.setRep((byte) 0, (byte) 0);
        t4ctyperep.setRep((byte) 1, (byte) 1);
        t4ctyperep.setRep((byte) 2, (byte) 1);
        t4ctyperep.setRep((byte) 3, (byte) 1);
        t4ctyperep.setRep((byte) 4, (byte) 1);
    }
}
