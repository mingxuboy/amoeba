/**
 * <pre>
 * Copyright 2004-2008 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com (&quot;Confidential
 * Information&quot;). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 * </pre>
 */
package com.meidusa.amoeba.oracle.net.packet;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:30:47
 */
public class T4CTTIoac {

    static final short UACFIND    = 1;
    static final short UACFALN    = 2;
    static final short UACFRCP    = 4;
    static final short UACFBBV    = 8;
    static final short UACFNCP    = 16;
    static final short UACFBLP    = 32;
    static final short UACFARR    = 64;
    static final short UACFIGN    = 128;
    static final short UACFNSCL   = 1;
    static final short UACFBUC    = 2;
    static final short UACFSKP    = 4;
    static final short UACFCHRCNT = 8;
    static final short UACFNOADJ  = 16;
    static final short UACFCUS    = 4096;
    static final byte  NO_BYTES[] = new byte[0];

    T4CPacketBuffer    meg;
    boolean            isStream;
    short              oacdty;
    short              oacflg;
    short              oacpre;
    short              oacscl;
    int                oacmxl;
    int                oacmxlc;
    int                oacmal;
    int                oacfl2;
    byte[]             oactoid;
    int                oactoidl;
    int                oacvsn;
    int                ncs;
    short              formOfUse;

    public T4CTTIoac(T4CPacketBuffer meg){
        this.meg = meg;
        this.oacmxlc = 0;
    }

    T4CTTIoac(T4CTTIoac t4cttioac){
        meg = t4cttioac.meg;
        oacmxlc = 0;
        isStream = t4cttioac.isStream;
        ncs = t4cttioac.ncs;
        formOfUse = t4cttioac.formOfUse;
        oacdty = t4cttioac.oacdty;
        oacflg = t4cttioac.oacflg;
        oacpre = t4cttioac.oacpre;
        oacscl = t4cttioac.oacscl;
        oacmxl = t4cttioac.oacmxl;
        oacmal = t4cttioac.oacmal;
        oacfl2 = t4cttioac.oacfl2;
        oactoid = t4cttioac.oactoid;
        oactoidl = t4cttioac.oactoidl;
        oacvsn = t4cttioac.oacvsn;
    }

    void initIbt(short word0, int i, int j) {
        oacflg = 67;
        oacpre = 0;
        oacscl = 0;
        oacmal = i;
        oacfl2 = 0;
        oacmxl = j;
        oactoid = null;
        oacvsn = 0;
        ncs = 0;
        formOfUse = 0;
        if (word0 == 9 || word0 == 96 || word0 == 1) {
            if (oacdty != 96) {
                oacdty = 1;
            }
            oacfl2 = 16;
        } else {
            oacdty = 2;
        }
    }

    void init(short word0, int i, short word1, short word2, short word3) {
        oacflg = 3;
        oacpre = 0;
        oacscl = 0;
        oacmal = 0;
        oacfl2 = 0;
        oacdty = word0;
        oacmxl = i;
        if (oacdty == 96 || oacdty == 9 || oacdty == 1) {
            if (oacdty != 96) {
                oacdty = 1;
            }
            oacfl2 = 16;
        } else if (oacdty == 104) {
            oacdty = 11;
        } else if (oacdty == 102) {
            oacmxl = 1;
        }
        oactoid = NO_BYTES;
        oactoidl = 0;
        oacvsn = 0;
        formOfUse = word3;
        ncs = word1;
        if (isNType()) {
            ncs = word2;
        }
    }

    void unmarshal() {
        oacdty = meg.unmarshalUB1();
        oacflg = meg.unmarshalUB1();
        oacpre = meg.unmarshalUB1();
        if (oacdty == 2 || oacdty == 180 || oacdty == 181 || oacdty == 231 || oacdty == 183) {
            oacscl = (short) meg.unmarshalUB2();
        } else {
            oacscl = meg.unmarshalUB1();
        }
        oacmxl = meg.unmarshalSB4();
        oacmal = meg.unmarshalSB4();
        oacfl2 = meg.unmarshalSB4();
        if (oacmxl > 0) switch (oacdty) {
            case 2:
                oacmxl = 22;
                break;
            case 12:
                oacmxl = 7;
                break;
            case 181:
                oacmxl = 13;
                break;
        }
        if (oacdty == 11) {
            oacdty = 104;
        }
        oactoid = meg.unmarshalDALC();
        oactoidl = oactoid != null ? oactoid.length : 0;
        oacvsn = meg.unmarshalUB2();
        ncs = meg.unmarshalUB2();
        formOfUse = meg.unmarshalUB1();
        if (T4CPacketBuffer.versionNumber >= 9000) {
            oacmxlc = (int) meg.unmarshalUB4();
        }
    }

    void marshal() {
        meg.marshalUB1(oacdty);// dataType
        meg.marshalUB1(oacflg);// flags
        meg.marshalUB1(oacpre);// precision
        meg.marshalUB1(oacscl);// scale
        meg.marshalUB4(oacmxl);// describeType
        meg.marshalSB4(oacmal);// total_elems
        meg.marshalSB4(oacfl2);// contflag
        meg.marshalDALC(oactoid);// TOID
        meg.marshalUB2(oacvsn);// TypeVersion
        meg.marshalUB2(ncs);// CharSet
        meg.marshalUB1(formOfUse);// CharSetForm
        if (T4CPacketBuffer.versionNumber >= 9000) {
            meg.marshalUB4(0L);
        }
    }

    boolean isNType() {
        return formOfUse == 2;
    }

    boolean isStream() {
        return isStream;
    }

}
