package com.meidusa.amoeba.mysql.context;

import com.meidusa.amoeba.context.RuntimeContext;
import com.meidusa.amoeba.mysql.util.CharsetMapping;

public class MysqlRuntimeContext extends RuntimeContext {
	public final static String SERVER_VERSION = "5.1.45-mysql-amoeba-proxy-2.0.0-BETA";
	private byte               serverCharsetIndex;

    public void setServerCharsetIndex(byte serverCharsetIndex) {
        this.serverCharsetIndex = serverCharsetIndex;
        this.setServerCharset(CharsetMapping.INDEX_TO_CHARSET[serverCharsetIndex & 0xff]);
    }

    public byte getServerCharsetIndex() {
        if (serverCharsetIndex > 0) return serverCharsetIndex;
        return CharsetMapping.getCharsetIndex(this.getServerCharset());
    }
}
