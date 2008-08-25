package com.meidusa.amoeba.oracle.net.packet;

import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-19 ÏÂÎç02:43:40
 */
public class T4CTTIoAuthKeyDataPacket extends T4CTTIfunPacket implements T4CTTIoAuth {

    
	public long   LOGON_MODE = 0L;
    public String user       = null;

    public String terminal   = null;
    public String program_nm = null;
    public String machine    = null;
    public String pid        = null;
    public String sid        = null;
    public Map<String,String> map;
    
    public T4CTTIoAuthKeyDataPacket(){
        super(OSESSKEY);
    }

    @Override
    protected void marshal(AbstractPacketBuffer buffer) {
        super.marshal(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalPTR();
        meg.marshalSB4(user.getBytes().length);
        meg.marshalUB4(LOGON_MODE | 1L);
        meg.marshalPTR();
        
        if(map == null){
        	map = genMap();
        }
        meg.marshalUB4(map.size());
        meg.marshalPTR();
        meg.marshalPTR();
        meg.marshalCHR(user.getBytes());
        meg.marshalMap(map);
    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.unmarshalUB1();
        int userLength = meg.unmarshalSB4();
        LOGON_MODE = meg.unmarshalUB4();
        meg.unmarshalUB1();
        int propLen = (int) meg.unmarshalUB4();// key-value length
        meg.unmarshalUB1();
        meg.unmarshalUB1();
        user = new String(meg.unmarshalCHR(userLength));
        map = meg.unmarshalMap(propLen);
    }

    private Map<String,String> genMap(){
    	Map<String,String> map = new HashMap<String,String>();
    	map.put(AUTH_TERMINAL, terminal);
    	map.put(AUTH_PROGRAM_NM, program_nm);
    	map.put(AUTH_MACHINE, machine);
    	map.put(AUTH_SID, sid);
    	map.put(AUTH_PID, pid);
    	return map;
    }
}
