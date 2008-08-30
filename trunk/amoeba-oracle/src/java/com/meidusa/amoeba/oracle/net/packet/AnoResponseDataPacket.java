package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-11 ÏÂÎç04:17:54
 */
public class AnoResponseDataPacket extends DataPacket implements AnoServices {

    int          m              = 0;
    long         version        = 0;
    int          anoServiceSize = 0; ;
    short        h              = 0;

    AnoService[] anoService;

    public void setAnoService(AnoService[] service) {
        anoService = new AnoService[service.length + 1];
        for (int i = 0; i < service.length; i++) {
            anoService[service[i].service] = service[i];
        }
    }

    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        OracleAbstractPacketBuffer buffer = (OracleAbstractPacketBuffer) absbuffer;
        if (buffer.readUB4() != DEADBEEF) {
            throw new RuntimeException("Wrong Magic number in na packet");
        }
        m = buffer.readUB2();
        version = buffer.readUB4();
        anoServiceSize = buffer.readUB2();
        h = buffer.readUB1();

        // // c
        // AnoService service = new AnoService();
        // for (int i = 0; i < anoServiceSize; i++) {
        // service.readServer();
        // anoService[service.service].b(service);
        // }
        //
        // // f
        // for (int i = 0; i < anoServiceSize; i++) {
        //
        // }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        AnoPacketBuffer buffer = (AnoPacketBuffer) absbuffer;
        buffer.writeUB4(NA_MAGIC);
        buffer.writeUB2(m);
        buffer.writeUB4(version);
        buffer.writeUB2(anoServiceSize);
        buffer.writeUB1(h);
        if (anoService == null && anoServiceSize > 0) {
            anoService = new AnoService[anoServiceSize];
            try {
                String pkgPrefix = "com.meidusa.amoeba.oracle.packet.";
                for (int i = 0; i < SERV_INORDER_CLASSNAME.length; i++) {
                    anoService[i] = (AnoService) Class.forName(pkgPrefix + SERV_INORDER_CLASSNAME[i]).newInstance();
                    anoService[i].doWrite(buffer);
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return AnoPacketBuffer.class;
    }
}
