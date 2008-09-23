package com.meidusa.amoeba.oracle.accessor;

abstract class CharCommonAccessor extends Accessor {

    static final int MAX_NB_CHAR_PLSQL = 32512;

    String getString(byte[] data) {
        String s = null;

        if (oac.formOfUse == 2) {
            s = conv.NCharBytesToString(data, data.length);
        } else {
            s = conv.CharBytesToString(data, data.length);
        }

        return s;
    }

}
