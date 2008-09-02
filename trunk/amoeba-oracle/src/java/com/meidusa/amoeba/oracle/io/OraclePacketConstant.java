package com.meidusa.amoeba.oracle.io;

public interface OraclePacketConstant {

    public static final int    HEADER_SIZE             = 8;
    public static final int    DATA_PACKET_HEADER_SIZE = 10;
    public static final int    DATA_OFFSET             = 10;
    public static final long   VERSION                 = 0x8105000L;
    public static final byte[] HEADER_PAD              = new byte[HEADER_SIZE];
}
