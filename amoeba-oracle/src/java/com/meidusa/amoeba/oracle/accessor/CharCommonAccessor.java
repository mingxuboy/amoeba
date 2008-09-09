package com.meidusa.amoeba.oracle.accessor;

abstract class CharCommonAccessor extends Accessor {

    static final int MAX_NB_CHAR_PLSQL = 32512;

    String getString(byte[] data) {
        String s = null;

        s = new String(data, 0, data.length);

        return s;
    }

}
