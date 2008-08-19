package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-19 ÏÂÎç02:43:29
 */
public class T4CTTIoAuthDataPacket extends T4CTTIfunPacket {

    int    userLength         = 0;
    long   LOGON_MODE         = 0L;
    byte[] user               = null;

    String auth_password      = "AUTH_PASSWORD";
    String password           = null;

    String auth_terminal      = "AUTH_TERMINAL";
    String terminal           = null;

    String auth_program_nm    = "AUTH_PROGRAM_NM";
    String program_nm         = null;

    String auth_machine       = "AUTH_MACHINE";
    String machine            = null;

    String auth_pid           = "AUTH_PID";
    String pid                = null;

    String auth_acl           = "AUTH_ACL";
    String acl                = null;

    String auth_alter_session = "AUTH_ALTER_SESSION";
    String alter_session      = null;

    String auth_copyright     = "AUTH_COPYRIGHT";
    String copyright          = "\"Oracle\nEverybody follows\nSpeedy bits exchange\nStars await to glow\"\nThe preceding key is copyrighted by Oracle Corporation.\nDuplication of this key is not allowed without permission\nfrom Oracle Corporation. Copyright 2003 Oracle Corporation.";

    public T4CTTIoAuthDataPacket(){
        this.funCode = OAUTH;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.unmarshalUB1();
        userLength = meg.unmarshalSB4();
        LOGON_MODE = meg.unmarshalUB4();
        meg.unmarshalUB1();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        meg.marshalPTR();
        meg.marshalSB4(userLength);
        meg.marshalUB4(LOGON_MODE | 1L | 256L);
        meg.marshalPTR();
    }

}
