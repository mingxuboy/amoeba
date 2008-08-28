package com.meidusa.amoeba.oracle.util;

public interface C06 {

    public abstract int takeSessionKey(byte abyte0[], byte abyte1[]);

    public abstract void renew();

    public abstract boolean compare(byte abyte0[], byte abyte1[]);

    public abstract byte[] compute(byte abyte0[], int i);

    public abstract void init(byte abyte0[], byte abyte1[]);

    public abstract int size();
}
