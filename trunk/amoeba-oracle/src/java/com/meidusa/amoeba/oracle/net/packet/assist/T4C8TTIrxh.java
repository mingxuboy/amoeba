package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:02:23
 */
public class T4C8TTIrxh {

    static final byte FU2O = 1;
    static final byte FEOR = 2;
    static final byte PLSV = 4;

    public short      flags;
    public int        numRqsts;
    public int        iterNum;
    public int        numItersThisTime;
    public int        uacBufLength;

    byte[]            abyte0;
    byte[]            abyte1;

    public void init() {
        flags = 0;
        numRqsts = 0;
        iterNum = 0;
        numItersThisTime = 0;
        uacBufLength = 0;
    }

    public void unmarshalV10(T4CTTIrxd rxd, T4CPacketBuffer meg) {
        flags = meg.unmarshalUB1();
        numRqsts = meg.unmarshalUB2();
        iterNum = meg.unmarshalUB2();
        numRqsts = numRqsts + iterNum * 256;
        numItersThisTime = meg.unmarshalUB2();
        uacBufLength = meg.unmarshalUB2();
        abyte0 = meg.unmarshalDALC();
        rxd.setNumberOfColumns(numRqsts);
        rxd.readBitVector(abyte0);
        abyte1 = meg.unmarshalDALC();
    }

    public void marshalV10(T4CPacketBuffer meg) {
        meg.marshalUB1(flags);
        meg.marshalUB2((numRqsts - iterNum * 256));
        meg.marshalUB2(iterNum);
        meg.marshalUB2(numItersThisTime);
        meg.marshalUB2(uacBufLength);
        meg.marshalDALC(abyte0);
        meg.marshalDALC(abyte1);
    }

}
