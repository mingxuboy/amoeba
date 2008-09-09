package com.meidusa.amoeba.oracle.accessor;

public class T4CNumberAccessor extends NumberCommonAccessor {

    @Override
    public Object getObject(byte[] dataBytes) {
        return super.getLong(dataBytes);
    }

}
