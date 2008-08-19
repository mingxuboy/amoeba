package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-19 下午02:43:18
 */
public class T4CTTIoAuthResponseDataPacket extends DataPacket implements T4CTTIoAuth {

    private static Logger logger = Logger.getLogger(T4CTTIoAuthResponseDataPacket.class);

    T4CTTIoer             oer    = null;

    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        oer = new T4CTTIoer(meg);

        byte[][] abyte0 = (byte[][]) null;
        byte[][] abyte1 = (byte[][]) null;
        int i = 0;
        while (true) {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 4:
                    oer.init();
                    oer.unmarshal();
                    if (oer.retCode != 0) {
                        String s = new String(oer.errorMsg);
                        logger.error(s);
                        return;
                    }
                    break;
                case 8:
                    i = meg.unmarshalUB2();
                    abyte0 = new byte[i][];
                    abyte1 = new byte[i][];
                    meg.unmarshalKEYVAL(abyte0, abyte1, i);
                    continue;
                case 15:
                    // oer.init();
                    // oer.unmarshalWarning();
                    // try {
                    // oer.processWarning();
                    // } catch (SQLWarning sqlwarning) {
                    // conn.setWarnings(DatabaseError.addSqlWarning(conn.getWarnings(), sqlwarning));
                    // }
                    // continue;
                default:
                    throw new RuntimeException("违反协议");
            }
            break;
        }

        for (int j = 0; j < i; j++) {
            String s = new String(abyte0[j]);
            String s1 = "";
            if (abyte1[j] != null) {
                s1 = new String(abyte1[j]);
            }
            // connectionValues.setProperty(s, s1);
        }

        return;
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

}
