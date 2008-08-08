package com.meidusa.amoeba.oracle.packet;

import org.apache.log4j.Logger;

public class AnoDataPacket extends DataPacket implements AnoServices {

    private static Logger logger = Logger.getLogger(AnoDataPacket.class);

    @Override
    public void init(byte[] buffer) {
        super.init(buffer);

        AnoPacketBuffer ano = new AnoPacketBuffer(buffer);
        ano.setPosition(pktOffset);

        long magic = ano.readUB4();
        if (magic != NA_MAGIC) {
            throw new RuntimeException("Wrong Magic number in na packet");
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AnoDataPacket info ==============================\n");
        return sb.toString();
    }

}
