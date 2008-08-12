package com.meidusa.amoeba.oracle.packet;

import org.apache.log4j.Logger;

/**
 * @author hexianmao
 * @version 2008-8-11 обнГ04:17:54
 */
public class AnoServerDataPacket extends DataPacket implements AnoServices {

    private static Logger logger = Logger.getLogger(AnoServerDataPacket.class);

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

    protected void init(AnoPacketBuffer buffer) {
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

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AnoServerDataPacket info ==============================\n");
        return sb.toString();
    }

}
