package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

class T4C8TTILobd {

    static final int   LOBD_STATE0     = 0;
    static final int   LOBD_STATE1     = 1;
    static final int   LOBD_STATE2     = 2;
    static final int   LOBD_STATE3     = 3;
    static final int   LOBD_STATE_EXIT = 4;
    static final short TTCG_LNG        = 254;
    static final short LOBDATALENGTH   = 252;
    static byte        ucs2Char[]      = new byte[2];

    // T4C8TTILobd(T4CMAREngine t4cmarengine){
    // //super((byte) 14);
    // setMarshalingEngine(t4cmarengine);
    // }

    void marshalLobData(byte abyte0[], long l, long l1, T4CPacketBuffer meg) {
        long l2 = l1;
        boolean flag = false;
        // marshalTTCcode();
        if (l2 > 252L) {
            flag = true;
            meg.marshalUB1((short) 254);
        }
        long l3 = 0L;
        for (; l2 > 252L; l2 -= 252L) {
            meg.marshalUB1((short) 252);
            meg.marshalB1Array(abyte0, (int) (l + l3 * 252L), 252);
            l3++;
        }

        if (l2 > 0L) {
            meg.marshalUB1((short) (int) l2);
            meg.marshalB1Array(abyte0, (int) (l + l3 * 252L), (int) l2);
        }
        if (flag) meg.marshalUB1((short) 0);
    }

    void marshalLobDataUB2(byte abyte0[], long l, long l1, T4CPacketBuffer meg) {
        long l2 = l1;
        boolean flag = false;
        // marshalTTCcode();
        if (l2 > 84L) {
            flag = true;
            meg.marshalUB1((short) 254);
        }
        long l3 = 0L;
        for (; l2 > 84L; l2 -= 84L) {
            meg.marshalUB1((short) 252);
            for (int i = 0; i < 84; i++) {
                meg.marshalUB1((short) 2);
                meg.marshalB1Array(abyte0, (int) (l + l3 * 168L + (long) (i * 2)), 2);
            }

            l3++;
        }

        if (l2 > 0L) {
            long l4 = l2 * 3L;
            meg.marshalUB1((short) (int) l4);
            for (int j = 0; (long) j < l2; j++) {
                meg.marshalUB1((short) 2);
                meg.marshalB1Array(abyte0, (int) (l + l3 * 168L + (long) (j * 2)), 2);
            }

        }
        if (flag) meg.marshalUB1((short) 0);
    }

    long unmarshalLobData(byte abyte0[], T4CPacketBuffer meg) {
        long l = 0L;
        long l1 = 0L;
        int i = 0;
        int j = 0;
        do {
            if (j != 4) {
                switch (j) {
                    case 0: // '\0'
                        i = meg.unmarshalUB1();
                        if (i == 254) {
                            j = 2;
                        } else {
                            j = 1;
                        }
                        break;

                    case 1: // '\001'
                        meg.getNBytes(abyte0, (int) l1, i);
                        l += i;
                        j = 4;
                        break;

                    case 2: // '\002'
                        i = meg.unmarshalUB1();
                        if (i > 0) {
                            j = 3;
                        } else {
                            j = 4;
                        }
                        break;

                    case 3: // '\003'
                        meg.getNBytes(abyte0, (int) l1, i);
                        l += i;
                        l1 += i;
                        j = 2;
                        break;
                }
            } else {
                return l;
            }

        } while (true);
    }

    long unmarshalClobUB2(byte abyte0[], T4CPacketBuffer meg) {
        long l = 0L;
        long l1 = 0L;
        int i = 0;
        int k1 = 0;
        do
            if (k1 != 4) switch (k1) {
                case 0: // '\0'
                    i = meg.unmarshalUB1();
                    if (i == 254) k1 = 2;
                    else k1 = 1;
                    break;

                case 1: // '\001'
                    for (int j = 0; j < i;) {
                        int i1 = meg.unmarshalUCS2(abyte0, l1);
                        j += i1;
                        l1 += 2L;
                    }

                    l += i;
                    k1 = 4;
                    break;

                case 2: // '\002'
                    i = meg.unmarshalUB1();
                    if (i > 0) k1 = 3;
                    else k1 = 4;
                    break;

                case 3: // '\003'
                    for (int k = 0; k < i;) {
                        int j1 = meg.unmarshalUCS2(abyte0, l1);
                        k += j1;
                        l1 += 2L;
                    }

                    l += i;
                    k1 = 2;
                    break;
            }
            else return l;
        while (true);
    }

}
