package com.meidusa.amoeba.oracle.packet;

import org.apache.log4j.Logger;

/**
 * @author hexianmao
 * @version 2008-8-11 ÏÂÎç04:18:18
 */
public class ConnectPacket extends AbstractPacket {

    private static Logger logger = Logger.getLogger(ConnectPacket.class);

    protected int         sdu;
    protected int         tdu;
    protected boolean     anoEnabled;
    protected byte[]      data;

    public void init(byte[] buffer) {
        super.init(buffer);
        sdu = buffer[14] & 0xff;
        sdu <<= 8;
        sdu |= buffer[15] & 0xff;
        tdu = buffer[16] & 0xff;
        tdu <<= 8;
        tdu |= buffer[17] & 0xff;
        dataLen = buffer[24] & 0xff;
        dataLen <<= 8;
        dataLen |= buffer[25] & 0xff;
        if (buffer[32] == 4 && buffer[33] == 4) {
            anoEnabled = false;
        } else {
            anoEnabled = true;
        }
        int dataOffSet = buffer[27];
        if (dataLen > 0) {
            data = new byte[dataLen];
            System.arraycopy(buffer, dataOffSet, data, 0, data.length);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ConnectPacket info ==============================\n");
        sb.append("sdu:").append(sdu).append("\n");
        sb.append("tdu:").append(tdu).append("\n");
        sb.append("anoEnabled:").append(anoEnabled).append("\n");
        sb.append("data:").append(new String(data)).append("\n");
        return sb.toString();
    }
}
