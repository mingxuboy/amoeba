package com.meidusa.amoeba.oracle.accessor;

public class T4CVarcharAccessor extends VarcharAccessor {

    public static String getString(byte[] dataBytes, int internalTypeMaxLength, int definedColumnSize) {
        String s = null;

        int j = 0;
        int k = dataBytes[j];
        if (k > internalTypeMaxLength) {
            k = internalTypeMaxLength;
        }
        s = new String(dataBytes, j + 1, k);

        if (s != null && definedColumnSize > 0 && s.length() > definedColumnSize) {
            s = s.substring(0, definedColumnSize);
        }
        return s;
    }

}
