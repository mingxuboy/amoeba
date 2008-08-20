package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 下午03:02:04
 */
public class T4C8OallDataPacket extends T4CTTIfunPacket {

    int                    cursor;
    final long[]           al8i4 = new long[13];

    int                    receiveState;
    byte                   sqlStmt[];
    boolean                plsql;
    boolean                sendBindsDefinition;
    boolean                sendDefines;
    int                    defCols;

    T4CTTIrxdDataPacket    rxd;
    T4C8TTIrxhDataPacket   rxh;
    T4CTTIoac              oac;
    T4CTTIdcbDataPacket    dcb;
    T4CTTIofetchDataPacket ofetch;
    T4CTTIoexecDataPacket  oexec;
    T4CTTIfobDataPacket    fob;

    public T4C8OallDataPacket(){
        this.funCode = OALL8;
        this.receiveState = 0;
        this.sqlStmt = new byte[0];
        this.plsql = false;
        this.sendBindsDefinition = false;
        this.sendDefines = false;
        this.defCols = 0;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        if (funCode == OFETCH) {
            ofetch = new T4CTTIofetchDataPacket();
            ofetch.init(buffer);
            this.cursor = ofetch.cursor;
            this.al8i4[1] = ofetch.al8i4_1;
        } else if (funCode == OALL8) {
            oexec = new T4CTTIoexecDataPacket();
            this.cursor = oexec.cursor;
            this.al8i4[1] = oexec.al8i4_1;
            
        } else {
            throw new RuntimeException("违反协议");
        }
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
    }

}
