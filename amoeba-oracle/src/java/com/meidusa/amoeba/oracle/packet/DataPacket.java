package com.meidusa.amoeba.oracle.packet;

import org.apache.log4j.Logger;

public class DataPacket extends AbstractPacket {

    private static Logger logger = Logger.getLogger(DataPacket.class);

    protected int         pktOffset;
    protected int         dataFlags;

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

        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("DataPacket info ==============================\n");
        return sb.toString();
    }
}
