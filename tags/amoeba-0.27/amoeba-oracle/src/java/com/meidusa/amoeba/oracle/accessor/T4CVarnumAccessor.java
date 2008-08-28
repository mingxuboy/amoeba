package com.meidusa.amoeba.oracle.accessor;

public class T4CVarnumAccessor extends VarnumAccessor {

    @Override
    public long getLong() {
        long l = 0L;
        byte[] abyte0 = rowSpaceByte;
        int j = 1;
        byte byte0 = abyte0[j - 1];
        byte byte1 = abyte0[j];
        long l1 = 0L;
        if ((byte1 & 0xffffff80) != 0) {
            if (byte1 == -128 && byte0 == 1) {
                return 0L;
            }
            byte byte2 = (byte) ((byte1 & 0xffffff7f) - 65);
            if (byte2 > 9) {
                throwOverflow();
            }
            if (byte2 == 9) {
                int k = 1;
                byte byte6 = byte0;
                if (byte0 > 11) {
                    byte6 = 11;
                }
                for (; k < byte6; k++) {
                    int k2 = abyte0[j + k] & 0xff;
                    int i4 = MAX_LONG[k];
                    if (k2 == i4) {
                        continue;
                    }
                    if (k2 < i4) {
                        break;
                    }
                    throwOverflow();
                }

                if (k == byte6 && byte0 > 11) {
                    throwOverflow();
                }
            }
            byte byte4 = (byte) (byte0 - 1);
            int i1 = (byte4 <= byte2 + 1) ? (byte4 + 1) : (byte2 + 2);
            int i2 = i1 + j;
            if (i1 > 1) {
                l1 = abyte0[j + 1] - 1;
                for (int l2 = 2 + j; l2 < i2; l2++) {
                    l1 = l1 * 100L + (long) (abyte0[l2] - 1);
                }
            }
            for (int i3 = byte2 - byte4; i3 >= 0; i3--) {
                l1 *= 100L;
            }
        } else {
            byte byte3 = (byte) ((~byte1 & 0xffffff7f) - 65);
            if (byte3 > 9) {
                throwOverflow();
            }
            if (byte3 == 9) {
                int j1 = 1;
                byte byte7 = byte0;
                if (byte0 > 12) {
                    byte7 = 12;
                }
                for (; j1 < byte7; j1++) {
                    int j3 = abyte0[j + j1] & 0xff;
                    int j4 = MIN_LONG[j1];
                    if (j3 == j4) {
                        continue;
                    }
                    if (j3 > j4) {
                        break;
                    }
                    throwOverflow();
                }

                if (j1 == byte7 && byte0 < 12) {
                    throwOverflow();
                }
            }
            byte byte5 = (byte) (byte0 - 1);
            if (byte5 != 20 || abyte0[j + byte5] == 102) {
                byte5--;
            }
            int k1 = byte5 <= byte3 + 1 ? byte5 + 1 : byte3 + 2;
            int j2 = k1 + j;
            if (k1 > 1) {
                l1 = 101 - abyte0[j + 1];
                for (int k3 = 2 + j; k3 < j2; k3++) {
                    l1 = l1 * 100L + (long) (101 - abyte0[k3]);
                }
            }
            for (int l3 = byte3 - byte5; l3 >= 0; l3--) {
                l1 *= 100L;
            }
            l1 = -l1;
        }
        l = l1;
        return l;
    }

    static void throwOverflow() {
        throw new RuntimeException("Êý×ÖÒç³ö");
    }

}
