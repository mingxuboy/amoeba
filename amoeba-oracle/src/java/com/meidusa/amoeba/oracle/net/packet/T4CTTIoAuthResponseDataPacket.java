package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-19 下午02:43:18
 */
public class T4CTTIoAuthResponseDataPacket extends DataPacket implements T4CTTIoAuth {

    private static Logger logger           = Logger.getLogger(T4CTTIoAuthResponseDataPacket.class);

    int                   len              = 0;
    T4CTTIoer             oer              = null;
    Properties            connectionValues = new Properties();

    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        oer = new T4CTTIoer(meg);

        byte[][] abyte0 = null;
        byte[][] abyte1 = null;
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
                    len = meg.unmarshalUB2();
                    abyte0 = new byte[len][];
                    abyte1 = new byte[len][];
                    meg.unmarshalKEYVAL(abyte0, abyte1, len);
                    continue;
                case 15:
                    oer.init();
                    oer.unmarshalWarning();
                    if (oer.retCode != 0) {
                        String s = new String(oer.errorMsg);
                        logger.warn(s);
                        // conn.setWarnings(DatabaseError.addSqlWarning(conn.getWarnings(), sqlwarning));
                        return;
                    }
                    continue;
                default:
                    throw new RuntimeException("违反协议");
            }
            break;
        }

        for (int j = 0; j < len; j++) {
            String s = new String(abyte0[j]);
            String s1 = "";
            if (abyte1[j] != null) {
                s1 = new String(abyte1[j]);
            }
            connectionValues.setProperty(s, s1);
        }

        return;
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.marshalUB1((byte) 8);
        meg.marshalUB2(len);

        byte[][] keys = new byte[len][];
        byte[][] vals = new byte[len][];
        byte[] abyte2 = new byte[len];

        int i = 0;
        keys[i] = AUTH_VERSION_STRING.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_VERSION_STRING)).getBytes();

        keys[i] = AUTH_VERSION_SQL.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_VERSION_SQL)).getBytes();

        keys[i] = AUTH_XACTION_TRAITS.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_XACTION_TRAITS)).getBytes();

        keys[i] = AUTH_VERSION_NO.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_VERSION_NO)).getBytes();

        keys[i] = AUTH_VERSION_STATUS.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_VERSION_STATUS)).getBytes();

        keys[i] = AUTH_CAPABILITY_TABLE.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_CAPABILITY_TABLE)).getBytes();

        keys[i] = AUTH_SESSION_ID.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_SESSION_ID)).getBytes();

        keys[i] = AUTH_SERIAL_NUM.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_SERIAL_NUM)).getBytes();

        keys[i] = AUTH_INSTANCE_NO.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_INSTANCE_NO)).getBytes();

        keys[i] = AUTH_NLS_LXLAN.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXLAN)).getBytes();

        keys[i] = AUTH_NLS_LXCTERRITORY.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCTERRITORY)).getBytes();

        keys[i] = AUTH_NLS_LXCCURRENCY.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCCURRENCY)).getBytes();

        keys[i] = AUTH_NLS_LXCISOCURR.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCISOCURR)).getBytes();

        keys[i] = AUTH_NLS_LXCNUMERICS.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCNUMERICS)).getBytes();

        keys[i] = AUTH_NLS_LXCDATEFM.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCDATEFM)).getBytes();

        keys[i] = AUTH_NLS_LXCDATELANG.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCDATELANG)).getBytes();

        keys[i] = AUTH_NLS_LXCSORT.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCSORT)).getBytes();

        keys[i] = AUTH_NLS_LXCCALENDAR.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCCALENDAR)).getBytes();

        keys[i] = AUTH_NLS_LXCUNIONCUR.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCUNIONCUR)).getBytes();

        keys[i] = AUTH_NLS_LXCTIMEFM.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCTIMEFM)).getBytes();

        keys[i] = AUTH_NLS_LXCSTMPFM.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCSTMPFM)).getBytes();

        keys[i] = AUTH_NLS_LXCTTZNFM.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCTTZNFM)).getBytes();

        keys[i] = AUTH_NLS_LXCSTZNFM.getBytes();
        vals[i++] = ((String) connectionValues.get(AUTH_NLS_LXCSTZNFM)).getBytes();

        meg.marshalKEYVAL(keys, vals, abyte2, len);

        meg.marshalUB1((byte) 4);
        oer.marshal(meg);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

}
