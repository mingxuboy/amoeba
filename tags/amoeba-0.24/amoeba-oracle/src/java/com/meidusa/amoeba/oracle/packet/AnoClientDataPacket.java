package com.meidusa.amoeba.oracle.packet;

import org.apache.log4j.Logger;

/**
 * @author hexianmao
 * @version 2008-8-11 ÏÂÎç04:17:45
 */
public class AnoClientDataPacket extends DataPacket implements AnoServices {

    private static Logger logger = Logger.getLogger(AnoClientDataPacket.class);

    int                   m;
    long                  version;
    int                   anoServiceSize;
    short                 h;
    AnoService[]          anoService;

    protected void init(AnoPacketBuffer buffer){
    	super.init(buffer);
    	if (buffer.readUB4() != NA_MAGIC) {
            throw new RuntimeException("Wrong Magic number in na packet");
        }
        m = buffer.readUB2();
        version = buffer.readUB4();
        anoServiceSize = buffer.readUB2();
        h = buffer.readUB1();

        anoService = new AnoService[anoServiceSize];
        try {
            String pkgPrefix = "com.meidusa.amoeba.oracle.packet.";
            for (int i = 0; i < SERV_INORDER_CLASSNAME.length; i++) {
                anoService[i] = (AnoService) Class.forName(pkgPrefix + SERV_INORDER_CLASSNAME[i]).newInstance();
                anoService[i].setAno(buffer);
                anoService[i].readClient();
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
    }
    
    public AnoService[] getAnoService() {
        return anoService;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AnoClientDataPacket info ==============================\n");
        return sb.toString();
    }

}
