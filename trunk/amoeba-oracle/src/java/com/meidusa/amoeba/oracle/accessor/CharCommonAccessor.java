package com.meidusa.amoeba.oracle.accessor;

abstract class CharCommonAccessor extends Accessor {

    static final int MAX_NB_CHAR_PLSQL = 32512;

    String getString(byte[] data) {
        String s = null;

        if (oac.formOfUse == 2) {
            s = oac.meg.getConversion().NCharBytesToString(data, data.length);
        } else {
            s = oac.meg.getConversion().CharBytesToString(data, data.length);
        }

        return s;
    }

}
