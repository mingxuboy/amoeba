package com.meidusa.amoeba.context;

import java.util.Map;

import com.meidusa.amoeba.config.DBServerConfig;

public interface DBServerConfigLoader {
	Map<String, DBServerConfig> loadConfig();
}
