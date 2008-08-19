package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.util.DBConversion;

/**
 * @author hexianmao
 * @version 2008-8-19 下午02:43:40
 */
public class T4CTTIoAuthKeyDataPacket extends T4CTTIfunPacket {

    int    userLength      = 0;
    long   LOGON_MODE      = 0L;
    int    propLen         = 0;
    byte[] user            = null;

    String auth_terminal   = "AUTH_TERMINAL";
    byte[] terminal        = null;

    String auth_program_nm = "AUTH_PROGRAM_NM";
    byte[] program_nm      = null;

    String auth_machine    = "AUTH_MACHINE";
    byte[] machine         = null;

    String auth_pid        = "AUTH_PID";
    byte[] pid             = null;

    String auth_sid        = "AUTH_SID";
    byte[] sid             = null;

    short  clientCharSetId = 1;

    public T4CTTIoAuthKeyDataPacket(){
        this.funCode = OSESSKEY;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        if (funCode != OSESSKEY) {
            throw new RuntimeException("违反协议");
        }
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.unmarshalUB1();
        userLength = meg.unmarshalSB4();
        LOGON_MODE = meg.unmarshalUB4();
        meg.unmarshalUB1();
        propLen = (int) meg.unmarshalUB4();// key-value length
        meg.unmarshalUB1();
        meg.unmarshalUB1();
        user = meg.unmarshalCHR(userLength);

        byte[][] keys = new byte[propLen][];
        byte[][] values = new byte[propLen][];
        meg.unmarshalKEYVAL(keys, values, propLen);

        if (propLen >= 4) {
            int i = 0;
            terminal = values[i++];
            if (propLen == 5) {
                program_nm = values[i++];
            }
            machine = values[i++];
            pid = values[i++];
            sid = values[i++];
        }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalPTR();
        meg.marshalSB4(user.length);
        meg.marshalUB4(LOGON_MODE | 1L);
        meg.marshalPTR();
        meg.marshalUB4(propLen);
        meg.marshalPTR();
        meg.marshalPTR();
        meg.marshalCHR(user);

        byte[][] keys = new byte[propLen][];
        byte[][] values = new byte[propLen][];
        byte[] abyte2 = new byte[propLen];
        try {
            int j = 0;
            keys[j] = DBConversion.stringToDriverCharBytes(auth_terminal, clientCharSetId);
            values[j++] = terminal;
            if (program_nm != null) {
                keys[j] = DBConversion.stringToDriverCharBytes(auth_program_nm, clientCharSetId);
                values[j++] = program_nm;
            }
            keys[j] = DBConversion.stringToDriverCharBytes(auth_machine, clientCharSetId);
            values[j++] = machine;
            keys[j] = DBConversion.stringToDriverCharBytes(auth_pid, clientCharSetId);
            values[j++] = pid;
            keys[j] = DBConversion.stringToDriverCharBytes(auth_sid, clientCharSetId);
            values[j++] = sid;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        meg.marshalKEYVAL(keys, values, abyte2, propLen);
    }

}
