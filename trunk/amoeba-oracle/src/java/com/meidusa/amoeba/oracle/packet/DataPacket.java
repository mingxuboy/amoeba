package com.meidusa.amoeba.oracle.packet;

public class DataPacket extends AbstractPacket {

    protected int pktOffset;
    protected int dataFlags;

    public void init(byte[] buffer) {
        super.init(buffer);
        dataOff = pktOffset = 10;
        dataLen = length - dataOff;
        dataFlags = buffer[8] & 0xff;
        dataFlags <<= 8;
        dataFlags |= buffer[9] & 0xff;
        if (type == 6 && (dataFlags & 0x40) != 0) {
            // sAtts.dataEOF = true;
        }
        if (type == 6 && 0 == dataLen)
            type = 7;
    }
}
