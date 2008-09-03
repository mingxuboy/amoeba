package com.meidusa.amoeba.oracle.accessor;

public class T4CVarcharAccessor extends VarcharAccessor {

    public static String getString(byte[] data, int maxLength, int columnSize) {
        String s = null;

        int k = data.length;
        if (k > maxLength) {
            k = maxLength;
        }
        s = new String(data, 0, k);

        if (s != null && columnSize > 0 && s.length() > columnSize) {
            s = s.substring(0, columnSize);
        }
        return s;
    }

}
