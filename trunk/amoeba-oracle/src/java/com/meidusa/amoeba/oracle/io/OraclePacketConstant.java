package com.meidusa.amoeba.oracle.io;

public interface OraclePacketConstant {
	public static final int HEADER_SIZE = 8;
	public static final int DATA_OFFSET = 10;
	public static final byte[] HEADER_PAD = new byte[HEADER_SIZE];
}
