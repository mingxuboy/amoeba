package com.meidusa.amoeba.oracle.accessor;

import java.io.CharArrayReader;
import java.io.Reader;

abstract class CharCommonAccessor extends Accessor {

    static final int MAX_NB_CHAR_PLSQL = 32512;

    protected char[] rowSpaceChar;

    public char[] getRowSpaceChar() {
        return rowSpaceChar;
    }

    public void setRowSpaceChar(char[] rowSpaceChar) {
        this.rowSpaceChar = rowSpaceChar;
    }

    String getString() {
        String s = null;

        int j = 0;
        int k = rowSpaceChar[j];// >> 1;
        if (k > internalTypeMaxLength) {
            k = internalTypeMaxLength;
        }
        s = new String(rowSpaceChar, j + 1, k);

        return s;
    }

    Reader getCharacterStream() {
        CharArrayReader chararrayreader = null;

        int j = 0;
        int k = rowSpaceChar[j] >> 1;
        if (k > internalTypeMaxLength) {
            k = internalTypeMaxLength;
        }
        chararrayreader = new CharArrayReader(rowSpaceChar, j + 1, k);

        return chararrayreader;
    }

}
