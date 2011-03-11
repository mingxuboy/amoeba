package com.meidusa.amoeba.context;

import java.util.Map;

import com.meidusa.amoeba.config.DBServerConfig;

public interface DBServerConfigLoader {
    public static final String             DEFAULT_REAL_POOL_CLASS                 = "com.meidusa.amoeba.net.poolable.PoolableObjectPool";
    public static final String             DEFAULT_VIRTUAL_POOL_CLASS              = "com.meidusa.amoeba.server.MultipleServerPool";
	Map<String, DBServerConfig> loadConfig();
}
