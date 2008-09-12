package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIoer;

/**
 * @author hexianmao
 * @version 2008-8-19 下午02:43:18
 */
public class T4CTTIoAuthResponseDataPacket extends DataPacket implements T4CTTIoAuth {

    private static Logger logger           = Logger.getLogger(T4CTTIoAuthResponseDataPacket.class);
    
    public T4CTTIoer             oer              = null;
    public Map<String,String> map;

    @Override
    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        oer = new T4CTTIoer();

        int len = 0;
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
                    len = meg.unmarshalUB2();
                    map = meg.unmarshalMap(len);
                    continue;
                case 15:
                    oer.init();
                    oer.unmarshalWarning(meg);
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
    }

    
    private Map<String,String> genConnectionProperties(){
    	Map<String,String> connectionValues = new HashMap<String,String>();
    	connectionValues.put(AUTH_VERSION_STRING, "- Production");
    	connectionValues.put(AUTH_VERSION_SQL, "18");
    	connectionValues.put(AUTH_XACTION_TRAITS, "3");
    	connectionValues.put(AUTH_VERSION_NO, "153093632");
    	connectionValues.put(AUTH_VERSION_STATUS, "0");
    	connectionValues.put(AUTH_CAPABILITY_TABLE, "");
    	
    	connectionValues.put(AUTH_SESSION_ID, "309");
    	connectionValues.put(AUTH_SERIAL_NUM, "59463");
    	
    	connectionValues.put(AUTH_INSTANCE_NO, "1");
    	connectionValues.put(AUTH_NLS_LXLAN, "");
    	connectionValues.put(AUTH_NLS_LXCTERRITORY, "");
    	connectionValues.put(AUTH_NLS_LXCCURRENCY, "");
    	connectionValues.put(AUTH_NLS_LXCISOCURR, "");
    	connectionValues.put(AUTH_NLS_LXCNUMERICS, "");
    	connectionValues.put(AUTH_NLS_LXCDATEFM, "");
    	connectionValues.put(AUTH_NLS_LXCDATELANG, "");
    	
    	connectionValues.put(AUTH_NLS_LXCSORT, "");
    	connectionValues.put(AUTH_NLS_LXCCALENDAR, "");
    	connectionValues.put(AUTH_NLS_LXCUNIONCUR, "");
    	
    	connectionValues.put(AUTH_NLS_LXCTIMEFM, "");
    	connectionValues.put(AUTH_NLS_LXCSTMPFM, "");
    	connectionValues.put(AUTH_NLS_LXCTTZNFM, "");
    	connectionValues.put(AUTH_NLS_LXCSTZNFM, "");
    	
    	return connectionValues;
    }
    
    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.marshalUB1((byte) 8);
        
        if(map == null){
        	map = genConnectionProperties();
        }
        int len = map.size();
        meg.marshalUB2(len);
        meg.marshalMap(map);
        
        meg.marshalUB1((byte) 4);
        if(oer == null){
        	oer = new T4CTTIoer();
        }
        oer.marshal(meg);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return T4CPacketBuffer.class;
    }

}
