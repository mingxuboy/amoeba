package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

/**
 * @author hexianmao
 * @version 2008-8-11 обнГ04:18:34
 */
public class DataPacket extends AbstractPacket {
	
	//protected int pktOffset;
    protected int dataFlags;

	public DataPacket(){
		this.type = 6;
	}
    

    /*public void init(byte[] buffer) {
        super.init(buffer);
        dataOff = pktOffset = 10;
        dataLen = length - dataOff;
        dataFlags = buffer[8] & 0xff;
        dataFlags <<= 8;
        dataFlags |= buffer[9] & 0xff;
        if (type == 6 && (dataFlags & 0x40) != 0) {
            // sAtts.dataEOF = true;
        }
        if (type == 6 && 0 == dataLen) {
            type = 7;
        }
    }*/
    
    protected void init(AnoPacketBuffer buffer){
    	super.init(buffer);
    	dataFlags = buffer.readUB2();
    }
    
	protected void write2Buffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeUB2(dataFlags);
	}

}
