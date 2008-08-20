package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:13:54
 */
public class T4CTTIrxdDataPacket extends T4CTTIMsgPacket {

    static final byte NO_BYTES[] = new byte[0];

    byte              buffer[];
    byte              bufferCHAR[];
    BitSet            bvcColSent;
    int               nbOfColumns;
    boolean           bvcFound;
    boolean           isFirstCol;

    public T4CTTIrxdDataPacket(){
        this.msgCode = TTIRXD;
        bvcColSent = null;
        nbOfColumns = 0;
        bvcFound = false;
        isFirstCol = true;
    }

    void init() {
        isFirstCol = true;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        // TODO Auto-generated method stub
        super.init(buffer);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        super.write2Buffer(buffer);
    }

}
