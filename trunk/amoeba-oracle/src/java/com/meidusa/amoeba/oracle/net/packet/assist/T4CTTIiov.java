package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

public class T4CTTIiov {

    static final byte BV_IN_V  = 32;
    static final byte BV_OUT_V = 16;

    T4C8TTIrxh        rxh;
    // T4CTTIrxd rxd;

    byte              bindtype;
    byte              iovector[];
    int               bindcnt;
    int               inbinds;
    int               outbinds;

    public T4CTTIiov(T4C8TTIrxh rxh){// , T4CTTIrxd rxd
        bindtype = 0;
        bindcnt = 0;
        inbinds = 0;
        outbinds = 0;

        this.rxh = rxh;
        // this.rxd = rxd;
    }

    public void init() {
    }

    public byte[] getIOVector() {
        return iovector;
    }

    public boolean isIOVectorEmpty() {
        return iovector.length == 0;
    }

    public void unmarshalV10(T4CPacketBuffer meg) {
        // rxh.unmarshalV10(rxd, meg);
        bindcnt = rxh.numRqsts;
        iovector = new byte[bindcnt];
        for (int i = 0; i < bindcnt; i++) {
            if ((bindtype = meg.unmarshalSB1()) == 0) {
                throw new RuntimeException("protocol error!");
            }
            if ((bindtype & 0x20) > 0) {
                iovector[i] |= 0x20;
                inbinds++;
            }
            if ((bindtype & 0x10) > 0) {
                iovector[i] |= 0x10;
                outbinds++;
            }
        }
    }

    // public Accessor[] processRXD(Accessor aaccessor[], int i, byte abyte0[], char ac[], short aword0[], int j,
    // DBConversion dbconversion, byte abyte1[], byte abyte2[], InputStream ainputstream[][],
    // byte abyte3[][][], OracleTypeADT aoracletypeadt[][], OracleStatement oraclestatement,
    // byte abyte4[], char ac1[], short aword1[]) {
    // if (abyte2 != null) {
    // for (int k = 0; k < abyte2.length; k++) {
    // if ((abyte2[k] & 0x10) != 0 && (aaccessor == null || aaccessor.length <= k || aaccessor[k] == null)) {
    // int l = j + 5 + 10 * k;
    // int i1 = aword0[l + 0] & 0xffff;
    // int j1 = i1;
    // if (i1 == 9) {
    // i1 = 1;
    // }
    // Accessor accessor = oraclestatement.allocateAccessor(i1, i1, k, 0, (short) 0, null, false);
    // accessor.rowSpaceIndicator = null;
    // if (accessor.defineType == 109 || accessor.defineType == 111) {
    // accessor.setOffsets(1);
    // }
    // if (aaccessor == null) {
    // aaccessor = new Accessor[k + 1];
    // aaccessor[k] = accessor;
    // continue;
    // }
    // if (aaccessor.length <= k) {
    // Accessor aaccessor1[] = new Accessor[k + 1];
    // aaccessor1[k] = accessor;
    // for (int k1 = 0; k1 < aaccessor.length; k1++) {
    // if (aaccessor[k1] != null) {
    // aaccessor1[k1] = aaccessor[k1];
    // }
    // }
    // aaccessor = aaccessor1;
    // } else {
    // aaccessor[k] = accessor;
    // }
    // continue;
    // }
    // if ((abyte2[k] & 0x10) == 0 && aaccessor != null && k < aaccessor.length && aaccessor[k] != null) {
    // aaccessor[k].isUseLess = true;
    // }
    // }
    //
    // }
    // return aaccessor;
    // }

}
