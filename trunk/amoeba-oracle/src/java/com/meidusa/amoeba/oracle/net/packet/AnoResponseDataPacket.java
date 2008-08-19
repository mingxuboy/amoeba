package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-11 ÏÂÎç04:17:54
 */
public class AnoResponseDataPacket extends DataPacket implements AnoServices {

    private static Logger logger = Logger.getLogger(AnoResponseDataPacket.class);

    int                   m;
    long                  version;
    int                   anoServiceSize;
    short                 h;

    AnoService[]          anoService;

    public void setAnoService(AnoService[] service) {
        anoService = new AnoService[service.length + 1];
        for (int i = 0; i < service.length; i++) {
            anoService[service[i].service] = service[i];
        }
    }

    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
    	OracleAbstractPacketBuffer buffer = (OracleAbstractPacketBuffer)absbuffer;
        super.init(buffer);
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

        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }

    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
    	AnoPacketBuffer buffer = (AnoPacketBuffer)absbuffer;
        super.write2Buffer(buffer);
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
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AnoServerDataPacket info ==============================\n");
        return sb.toString();
    }

    @Override
	protected Class<? extends AbstractPacketBuffer> getBufferClass() {
		return OracleAbstractPacketBuffer.class;
	}
}
