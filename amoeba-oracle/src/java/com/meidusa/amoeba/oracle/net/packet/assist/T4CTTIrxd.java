package com.meidusa.amoeba.oracle.net.packet.assist;

import java.util.BitSet;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 下午02:13:54
 */
public class T4CTTIrxd {

    static final byte[]  NO_BYTES     = new byte[0];
    static final short[] byteIndicate = { 0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80 };

    BitSet               bvcColSent;
    int                  nbOfColumns;
    boolean              bvcFound;
    boolean              isFirstCol;

    public T4CTTIrxd(){
        bvcColSent = null;
        nbOfColumns = 0;
        bvcFound = false;
        isFirstCol = true;
    }

    public void init() {
        isFirstCol = true;
    }

    public void setNumberOfColumns(int i) {
        nbOfColumns = i;
        bvcFound = false;
        bvcColSent = new BitSet(nbOfColumns);
    }

    public void unmarshalBVC(int i, T4CPacketBuffer meg) {
        int j = 0;
        for (int k = 0; k < bvcColSent.length(); k++) {
            bvcColSent.clear(k);
        }

        int l = nbOfColumns / 8 + (nbOfColumns % 8 == 0 ? 0 : 1);
        for (int i1 = 0; i1 < l; i1++) {
            byte byte0 = (byte) (meg.unmarshalUB1() & 0xff);
            for (int j1 = 0; j1 < 8; j1++) {
                if ((byte0 & (1 << j1)) != 0) {
                    bvcColSent.set(i1 * 8 + j1);
                    j++;
                }
            }
        }

        if (j != i) {
            throw new RuntimeException("INTERNAL ERROR: T4CTTIrxd.unmarshalBVC: bits missing in vector");
        }
        bvcFound = true;
    }

    public void readBitVector(byte abyte0[]) {
        for (int i = 0; i < bvcColSent.length(); i++) {
            bvcColSent.clear(i);
        }

        if (abyte0 == null || abyte0.length == 0) {
            bvcFound = false;
        } else {
            for (int j = 0; j < abyte0.length; j++) {
                byte byte0 = abyte0[j];
                for (int k = 0; k < 8; k++) {
                    if ((byte0 & 1 << k) != 0) {
                        bvcColSent.set(j * 8 + k);
                    }
                }
            }
            bvcFound = true;
        }
    }

    /**
     * 取得列对应的物理位置
     */
    public static int[] getColsPosition(int nbOfCols, byte[] indicate) {
        if (indicate == null || indicate.length == 0) {
            return null;
        } else {
            int cursor = 0;
            int[] count = new int[nbOfCols];
            for (int j = 0; j < indicate.length; j++) {
                byte byte0 = indicate[j];
                for (int k = 0; k < byteIndicate.length; k++) {
                    if ((byte0 & byteIndicate[k]) != 0) {
                        count[cursor++] = j * 8 + k;
                    }
                }
            }
            if (nbOfCols != cursor) {
                throw new RuntimeException("readColumnsPosition error,bits missing in indicate");
            } else {
                return count;
            }
        }
    }

}
