package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-19 下午02:43:47
 */
public class T4CTTIoAuthKeyResponseDataPacket extends DataPacket implements T4CTTIoAuth {

    private static Logger      logger      = Logger.getLogger(T4CTTIoAuthKeyResponseDataPacket.class);

    public String              encryptedSK = null;
    public T4CTTIoer           oer         = null;
    public Map<String, String> map;

    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        oer = new T4CTTIoer();

        while (true) {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 4:
                    oer.init();
                    oer.unmarshal(meg);
                    if (oer.retCode != 0) {
                        String s = new String(oer.errorMsg);
                        logger.error(s);
                        return;
                    }
                    break;
                case 8:
                    int len = meg.unmarshalUB2();
                    map = meg.unmarshalMap(len);
                    continue;
                default:
                    throw new RuntimeException("违反协议");
            }
            break;
        }

        if (map == null || map.size() < 1) {
            throw new RuntimeException("内部 - 不期望的值");
        }
        encryptedSK = map.get(AUTH_SESSKEY);
        if (encryptedSK == null || encryptedSK.length() != 16) {
            throw new RuntimeException("内部 - 不期望的值");
        }
        return;
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        if (oer == null) {
            oer = new T4CTTIoer();
        }

        if (map == null) {
            map = genMap();
        }
        meg.marshalUB1((byte) 8);
        meg.marshalUB2(map.size());
        meg.marshalMap(map);

        meg.marshalUB1((byte) 4);
        oer.marshal(meg);
    }

    private Map<String, String> genMap() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AUTH_SESSKEY, encryptedSK);
        return properties;
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return T4CPacketBuffer.class;
    }

}
