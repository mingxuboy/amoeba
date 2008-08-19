package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

public class T4CTTIoAuthKeyResponseDataPacket extends DataPacket {

    private static Logger logger      = Logger.getLogger(T4CTTIoAuthKeyResponseDataPacket.class);

    int                   len         = 0;
    byte[][]              abyte0      = null;
    byte[][]              abyte1      = null;
    byte[]                encryptedSK = null;

    T4CTTIoer             oer         = null;

    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        new T4CTTIoer(meg);

        while (true) {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 4:
                    oer.init();
                    oer.unmarshal();
                    if (oer.errorMsg.length > 0) {
                        String s = new String(oer.errorMsg);
                        logger.error(s);
                    }
                    break;
                case 8:
                    len = meg.unmarshalUB2();
                    abyte0 = new byte[len][];
                    abyte1 = new byte[len][];
                    meg.unmarshalKEYVAL(abyte0, abyte1, len);
                    continue;
                default:
                    throw new RuntimeException("违反协议");
            }
            break;
        }

        if (abyte0 == null || abyte0.length < 1) {
            throw new RuntimeException("内部 - 不期望的值");
        }
        encryptedSK = abyte1[0];
        if (encryptedSK == null || encryptedSK.length != 16) {
            throw new RuntimeException("内部 - 不期望的值");
        }
        return;
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.marshalUB1((byte) 8);
        meg.marshalUB2(len);
        meg.marshalKEYVAL(abyte0, abyte1, new byte[len], len);
        meg.marshalUB1((byte) 4);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

}
