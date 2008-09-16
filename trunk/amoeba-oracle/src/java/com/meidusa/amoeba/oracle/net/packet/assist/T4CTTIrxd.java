package com.meidusa.amoeba.oracle.net.packet.assist;

import java.util.BitSet;

/**
 * @author hexianmao
 * @version 2008-8-20 обнГ02:13:54
 */
public class T4CTTIrxd {

    static final byte NO_BYTES[] = new byte[0];

    byte[]            buffer;
    byte[]            bufferCHAR;
    BitSet            bvcColSent;
    int               nbOfColumns;
    boolean           bvcFound;
    boolean           isFirstCol;

    public T4CTTIrxd(){
        bvcColSent = null;
        nbOfColumns = 0;
        bvcFound = false;
        isFirstCol = true;
    }

    void init() {
        isFirstCol = true;
    }

}
