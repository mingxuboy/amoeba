package com.meidusa.amoeba.oracle.net.packet.assist;

import java.util.BitSet;

import com.meidusa.amoeba.oracle.accessor.Accessor;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:13:54
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
        // super(TTIRXD);
        // bvcColSent = null;
        // nbOfColumns = 0;
        // bvcFound = false;
        // isFirstCol = true;
    }

    void init() {
        isFirstCol = true;
    }

    boolean unmarshal(Accessor[] aaccessor, int j) {
        return unmarshal(aaccessor, 0, j);
    }

    boolean unmarshal(Accessor[] aaccessor, int i, int j) {
        if (i == 0) {
            isFirstCol = true;
        }
        // for (int k = i; k < j && k < aaccessor.length; k++) {
        // if (aaccessor[k] == null) {
        // continue;
        // }
        //
        // if (aaccessor[k].physicalColumnIndex < 0) {
        // int l = 0;
        // for (int i1 = 0; i1 < j && i1 < aaccessor.length; i1++) {
        // if (aaccessor[i1] == null) {
        // continue;
        // }
        // aaccessor[i1].physicalColumnIndex = l;
        // if (!aaccessor[i1].isUseLess) {
        // l++;
        // }
        // }
        // }
        //
        // if (bvcFound && !aaccessor[k].isUseLess) {
        // if (bvcColSent.get(aaccessor[k].physicalColumnIndex)) {
        // if (aaccessor[k].unmarshalOneRow()) {
        // return true;
        // }
        // isFirstCol = false;
        // } else {
        // aaccessor[k].copyRow();
        // }
        // continue;
        // }
        //
        // if (aaccessor[k].unmarshalOneRow()) {
        // return true;
        // }
        //
        // isFirstCol = false;
        // }

        bvcFound = false;
        return false;
    }

}
