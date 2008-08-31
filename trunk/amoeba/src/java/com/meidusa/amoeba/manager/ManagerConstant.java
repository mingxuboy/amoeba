package com.meidusa.amoeba.manager;

public interface ManagerConstant {
	int HEADER_SIZE = 4;
	byte FUN_TYPE_OBJECT = 1;
	byte FUN_TYPE_PING = 2;
	byte FUN_TYPE_OK = 3;
	byte[] HEADER_PAD = new byte[HEADER_SIZE];
}
