package com.meidusa.amoeba.oracle.net.packet;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import oracle.security.o3logon.O3LoginClientHelper;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.charset.CharacterSetMetaData;
import com.meidusa.amoeba.oracle.util.DBConversion;
import com.meidusa.amoeba.oracle.util.RepConversion;

/**
 * @author hexianmao
 * @version 2008-8-19 ÏÂÎç02:43:29
 */
public class T4CTTIoAuthDataPacket extends T4CTTIfunPacket implements T4CTTIoAuth {

    int                 userLength         = 0;
    public long         LOGON_MODE         = 0L;
    public String       userStr            = null;
    public long         logonMode;
    public String       passwordStr        = "ccbutest1";
    public String       terminal           = null;
    public String       programName        = null;
    public String       machine            = null;
    private String      ressourceManagerId = "0000";
    public String       encryptedSK;
    public String       clientname;
    public String       processID;
    public String       internalName;
    public String       externalName;
    public String       aclValue;
    public String       alterSession;
    public String       sysUserName;
    public short        versionNumber;
    public Map<String, String> map                = null;
    public String encryptedPassword = null;
    
    
    public T4CTTIoAuthDataPacket(){
        super(OAUTH);
        initFields();
    }

    public static String encryptPassword(String userStr,String passwordStr,byte[] encryptedSK,DBConversion conversion){
    	O3LoginClientHelper o3loginclienthelper = new O3LoginClientHelper(conversion.isServerCSMultiByte);
        byte abyte2[] = o3loginclienthelper.getSessionKey(userStr, passwordStr, encryptedSK);
        byte abyte0[] = conversion.StringToCharBytes(passwordStr);
        byte byte0;
        if (abyte0.length % 8 > 0) byte0 = (byte) (8 - abyte0.length % 8);
        else byte0 = 0;
        byte abyte1[] = new byte[abyte0.length + byte0];
        System.arraycopy(abyte0, 0, abyte1, 0, abyte0.length);
        byte abyte3[] = o3loginclienthelper.getEPasswd(abyte2, abyte1);
        byte[] password = new byte[2 * abyte1.length + 1];
        /*
         * if (password.length < 2 abyte3.length) DatabaseError.throwSqlException(413);
         */
        RepConversion.bArray2Nibbles(abyte3, password);
        password[password.length - 1] = RepConversion.nibbleToHex(byte0);
        return new String(password);
    }
    
    @Override
    protected void marshal(AbstractPacketBuffer buffer) {
        super.marshal(buffer);

        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        /*O3LoginClientHelper o3loginclienthelper = new O3LoginClientHelper(meg.getConversion().isServerCSMultiByte);
        byte abyte2[] = o3loginclienthelper.getSessionKey(userStr, passwordStr, encryptedSK);
        byte abyte0[] = meg.getConversion().StringToCharBytes(passwordStr);
        byte byte0;
        if (abyte0.length % 8 > 0) byte0 = (byte) (8 - abyte0.length % 8);
        else byte0 = 0;
        byte abyte1[] = new byte[abyte0.length + byte0];
        System.arraycopy(abyte0, 0, abyte1, 0, abyte0.length);
        byte abyte3[] = o3loginclienthelper.getEPasswd(abyte2, abyte1);
        byte[] password = new byte[2 * abyte1.length + 1];
        
         * if (password.length < 2 abyte3.length) DatabaseError.throwSqlException(413);
         
        RepConversion.bArray2Nibbles(abyte3, password);
        password[password.length - 1] = RepConversion.nibbleToHex(byte0);*/
        
        meg.marshalPTR();
        byte[] user = meg.getConversion().StringToCharBytes(userStr);
        meg.marshalSB4(user.length);
        meg.marshalUB4(logonMode | 1L | 256L);
        meg.marshalPTR();
        boolean flag1 = false;
        if (!ressourceManagerId.equals("0000")) flag1 = true;
        int i = 6;
        if (flag1) i += 2;
        i++;
        if (programName != null) i++;
        if (clientname != null) i++;
        meg.marshalUB4(i);
        meg.marshalPTR();
        meg.marshalPTR();
        meg.marshalCHR(user);
        Map<String, String> map = generateMap();
        
        String password = encryptPassword(userStr,passwordStr,encryptedSK.getBytes(),meg.getConversion());
        
        map.put("AUTH_PASSWORD", password);
        meg.marshalMap(map);
    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.unmarshalUB1();
        userLength = meg.unmarshalSB4();
        LOGON_MODE = meg.unmarshalUB4();
        meg.unmarshalUB1();
        long keyValuePaire = meg.unmarshalUB4();
        meg.unmarshalUB1();
        meg.unmarshalUB1();
        userStr = new String(meg.unmarshalCHR(userLength));
        map = meg.unmarshalMap((int) keyValuePaire);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey());
        }
    }

    private Map<String, String> generateMap() {
        Map<String, String> map = new HashMap<String, String>();
        // map.put("AUTH_PASSWORD", new String(password));
        map.put(AUTH_MACHINE, machine);
        map.put(AUTH_PID, processID);
        map.put(AUTH_ACL, aclValue);
        byte[] aler = alterSession.getBytes();
        aler[aler.length - 1] = 0;
        map.put(AUTH_ALTER_SESSION, new String(aler));
        map.put(AUTH_COPYRIGHT, COPYRIGHT_STR);
        map.put(AUTH_PROGRAM_NM, programName);
        map.put(AUTH_TERMINAL, terminal);

        return map;
    }

    protected void initFields() {
        terminal = "unknown";
        try {
            machine = InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            machine = "jdbcclient";
        }
        sysUserName = System.getProperty("user.name");
        programName = "Amoeba proxy client";
        processID = "1234";
        internalName = "jdbc_ttc_impl";
        externalName = "jdbc_" + ressourceManagerId;

        TimeZone timezone = TimeZone.getDefault();
        int i = timezone.getRawOffset();
        int j = i / 0x36ee80;
        int k = (i / 60000) % 60;
        if (timezone.useDaylightTime() && timezone.inDaylightTime(new Date())) j++;
        String s8 = (j >= 0 ? "+" + j : "" + j) + (k >= 10 ? ":" + k : ":0" + k);
        String s9 = CharacterSetMetaData.getNLSLanguage(Locale.getDefault());
        String s10 = CharacterSetMetaData.getNLSTerritory(Locale.getDefault());
        /*
         * if (s9 == null) DatabaseError.throwSqlException(176);
         */
        alterSession = "ALTER SESSION SET " + ((versionNumber >= 8100) ? "TIME_ZONE='" + s8 + "'" : "")
                       + " NLS_LANGUAGE='" + s9 + "' NLS_TERRITORY='" + s10 + "' ";
        aclValue = "4400";
    }

}
