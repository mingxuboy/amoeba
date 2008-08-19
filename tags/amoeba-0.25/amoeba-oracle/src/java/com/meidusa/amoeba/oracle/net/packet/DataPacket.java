package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-11 ÏÂÎç04:18:34
 */
public abstract class DataPacket extends AbstractPacket {

    protected int dataFlags;

    public DataPacket(){
        this.type = NS_PACKT_TYPE_DATA;
    }

//    public void init(byte[] buffer) {
//        super.init(buffer);
//        dataOff = pktOffset = 10;
//        dataLen = length - dataOff;
//        dataFlags = buffer[8] & 0xff;
//        dataFlags <<= 8;
//        dataFlags |= buffer[9] & 0xff;
//        if (type == 6 && (dataFlags & 0x40) != 0) {
//            // sAtts.dataEOF = true;
//        }
//        if (type == 6 && 0 == dataLen) {
//            type = 7;
//        }
//    }

    
    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        OracleAbstractPacketBuffer buffer = (OracleAbstractPacketBuffer)absbuffer;
        dataFlags = buffer.readUB2();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        OracleAbstractPacketBuffer buffer = (OracleAbstractPacketBuffer)absbuffer;
        buffer.writeUB2(dataFlags);
    }

    @Override
	protected Class<? extends AbstractPacketBuffer> getBufferClass() {
		return OracleAbstractPacketBuffer.class;
	}
}
