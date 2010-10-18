package com.meidusa.amoeba.context;

import java.util.Map;

import com.meidusa.amoeba.config.DBServerConfig;

public class DBServerConfigFileLoader implements DBServerConfigLoader {
	
	private String configFile;
	public String getConfigFile() {
		return configFile;
	}
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	@Override
	public Map<String, DBServerConfig> loadConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
