package com.meidusa.amoeba.oracle.accessor;

public class T4CDateAccessor extends DateTimeCommonAccessor {

    @Override
    public Object getObject(byte[] dataBytes) {
        return super.getDate(dataBytes);
    }

}
