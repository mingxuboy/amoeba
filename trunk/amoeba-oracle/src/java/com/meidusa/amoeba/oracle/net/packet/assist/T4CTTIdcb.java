package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 下午02:16:11
 */
public class T4CTTIdcb {

    T4C8TTIuds[] uds;
    int          numuds;
    String[]     colnames;
    int          colOffset;
    byte[]       ignoreBuff;
    StringBuffer colNameSB;

    long         skip;
    short        skip2;
    byte[]       skip3;

    public T4CTTIdcb(){
        // super(TTIDCB);
        ignoreBuff = new byte[40];
    }

    public void init(int colOffset) {
        this.colOffset = colOffset;
    }

    public int getNumuds() {
        return numuds;
    }

    public void unmarshal(T4CPacketBuffer meg) {
        short word0 = meg.unmarshalUB1();
        if (ignoreBuff.length < word0) {
            ignoreBuff = new byte[word0];
        }
        meg.unmarshalNBytes(ignoreBuff, 0, word0);
        skip = meg.unmarshalUB4();// skip read
        receiveCommon(false, meg);
    }

    public void receiveCommon(boolean flag, T4CPacketBuffer meg) {
        if (flag) {
            numuds = meg.unmarshalUB2();
        } else {
            numuds = (int) meg.unmarshalUB4();
            if (numuds > 0) {
                skip2 = meg.unmarshalUB1();
            }
        }

        uds = new T4C8TTIuds[numuds];
        colnames = new String[numuds];
        for (int i = 0; i < numuds; i++) {
            uds[i] = new T4C8TTIuds();
            uds[i].unmarshal(meg);
            if (meg.versionNumber >= 10000) {
                meg.unmarshalUB2();
            }
            colnames[i] = meg.getConversion().CharBytesToString(uds[i].getColumName(), uds[i].getColumNameByteLength());
        }

        if (!flag) {
            skip3 = meg.unmarshalDALC();
            if (meg.versionNumber >= 10000) {
                meg.unmarshalUB4();
                meg.unmarshalUB4();
            }
        }
    }

    public void marshal(T4CPacketBuffer meg, boolean flag) {
        // if (ignoreBuff != null && ignoreBuff.length > 0) {
        // meg.marshalCLR(ignoreBuff, ignoreBuff.length);
        // } else {
        // meg.marshalNULLPTR();
        // }
        meg.marshalNULLPTR();// ignoreBuff使用0代替

        meg.marshalUB4(skip);
        if (flag) {
            meg.marshalUB2(numuds);
        } else {
            meg.marshalUB4(numuds);
            if (numuds > 0) {
                meg.marshalUB1(skip2);
            }
        }
        for (int i = 0; i < numuds; i++) {
            uds[i].marshal(meg);
            if (meg.versionNumber >= 10000) {
                meg.marshalUB2(0);
            }
        }

        if (!flag) {
            meg.marshalDALC(skip3);
            if (meg.versionNumber >= 10000) {
                meg.marshalUB4(0L);
                meg.marshalUB4(0L);
            }
        }

    }

}
