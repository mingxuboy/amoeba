package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-19 下午02:43:40
 */
public class T4CTTIoAuthKeyDataPacket extends T4CTTIfunPacket {

    int    userLength      = 0;
    long   LOGON_MODE      = 0L;
    int    propLen         = 0;
    String user            = null;

    String auth_terminal   = "AUTH_TERMINAL";
    String terminal        = null;

    String auth_program_nm = "AUTH_PROGRAM_NM";
    String program_nm      = null;

    String auth_machine    = "AUTH_MACHINE";
    String machine         = null;

    String auth_pid        = "AUTH_PID";
    String pid             = null;

    String auth_sid        = "AUTH_SID";
    String sid             = null;

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
        user = new String(meg.unmarshalCHR(userLength));

        byte[][] keys = new byte[propLen][];
        byte[][] values = new byte[propLen][];
        meg.unmarshalKEYVAL(keys, values, propLen);

        if (propLen >= 4) {
            int i = 0;
            terminal = new String(values[i++]);
            if (propLen == 5) {
                program_nm = new String(values[i++]);
            }
            machine = new String(values[i++]);
            pid = new String(values[i++]);
            sid = new String(values[i++]);
        }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalPTR();
        meg.marshalSB4(userLength);
        meg.marshalUB4(LOGON_MODE | 1L);
        meg.marshalPTR();
        meg.marshalUB4(propLen);
        meg.marshalPTR();
        meg.marshalPTR();
        meg.marshalCHR(user.getBytes());

        byte[][] keys = new byte[propLen][];
        byte[][] values = new byte[propLen][];
        byte[] abyte2 = new byte[propLen];
        int j = 0;
        keys[j] = auth_terminal.getBytes();
        values[j++] = terminal.getBytes();
        if (program_nm != null) {
            keys[j] = auth_program_nm.getBytes();
            values[j++] = program_nm.getBytes();
        }
        keys[j] = auth_machine.getBytes();
        values[j++] = machine.getBytes();
        keys[j] = auth_pid.getBytes();
        values[j++] = pid.getBytes();
        keys[j] = auth_sid.getBytes();
        values[j++] = sid.getBytes();

        meg.marshalKEYVAL(keys, values, abyte2, propLen);
    }

}
