package com.meidusa.amoeba.oracle.accessor;

import java.io.CharArrayReader;
import java.io.Reader;

abstract class CharCommonAccessor extends Accessor {

    static final int MAX_NB_CHAR_PLSQL = 32512;

    protected int    internalTypeMaxLength;
    protected int    definedColumnSize;

    public int getInternalTypeMaxLength() {
        return internalTypeMaxLength;
    }

    public void setInternalTypeMaxLength(int internalTypeMaxLength) {
        this.internalTypeMaxLength = internalTypeMaxLength;
    }

    public int getDefinedColumnSize() {
        return definedColumnSize;
    }

    public void setDefinedColumnSize(int definedColumnSize) {
        this.definedColumnSize = definedColumnSize;
    }

    String getString() {
        String s = null;

        int j = 0;
        int k = dataBytes[j];
        if (k > internalTypeMaxLength) {
            k = internalTypeMaxLength;
        }
        s = new String(dataBytes, j + 1, k);

        return s;
    }

    Reader getCharacterStream() {
        CharArrayReader chararrayreader = null;

        int j = 0;
        int k = dataBytes[j];
        if (k > internalTypeMaxLength) {
            k = internalTypeMaxLength;
        }
        chararrayreader = new CharArrayReader(getString().toCharArray(), j + 1, k);

        return chararrayreader;
    }

}
