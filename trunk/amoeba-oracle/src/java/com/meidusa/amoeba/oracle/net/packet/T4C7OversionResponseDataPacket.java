package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIoer;

/**
 * 数据库版本信息数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:32:33
 */
public class T4C7OversionResponseDataPacket extends DataPacket {

    public String rdbmsVersion = "Oracle9i Enterprise Edition Release 9.2.0.6.0 - Production \n With the Partitioning option \n JServer Release 9.2.0.6.0 - Production";
    public long   retVerNum    = 153093632L;                                                                                                                            // 9260

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        boolean flag = false;
        T4CTTIoer oer = new T4CTTIoer();
        while (true) {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 4:
                    oer.init();
                    oer.unmarshal(meg);
                    // oer.processError();
                    break;
                case 8:
                    if (flag) {
                        // DatabaseError.throwSqlException(401);
                    }
                    int retVerLen = meg.unmarshalUB2();
                    byte[] vers = meg.unmarshalCHR(retVerLen);
                    try {
                        rdbmsVersion = new String(vers, "UTF8");
                    } catch (UnsupportedEncodingException e) {
                        rdbmsVersion = new String(vers);
                    }
                    if (rdbmsVersion == null) {
                        // DatabaseError.throwSqlException(438);
                    }
                    retVerNum = meg.unmarshalUB4();
                    // meg.versionNumber = getVersionNumber();
                    flag = true;
                    continue;
                case 9:
                    if (getVersionNumber() >= 10000) {
                        short word0 = (short) meg.unmarshalUB2();
                        // connection.endToEndECIDSequenceNumber = word0;
                    }
                    break;
                default: {
                    // DatabaseError.throwSqlException(401);
                }
            }
            break;
        }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalUB1((byte) 8);
        meg.marshalUB2(rdbmsVersion.length());
        meg.marshalCHR(rdbmsVersion.getBytes());
        meg.marshalUB4(retVerNum);
        meg.marshalUB1((byte) 9);
    }

    public short getVersionNumber() {
        int i = 0;
        i = (int) ((long) i + (retVerNum >>> 24 & 255L) * 1000L);
        i = (int) ((long) i + (retVerNum >>> 20 & 15L) * 100L);
        i = (int) ((long) i + (retVerNum >>> 12 & 15L) * 10L);
        i = (int) ((long) i + (retVerNum >>> 8 & 15L));
        return (short) i;
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return T4CPacketBuffer.class;
    }
}
