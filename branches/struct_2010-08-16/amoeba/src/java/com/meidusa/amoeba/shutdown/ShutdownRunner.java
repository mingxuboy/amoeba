package com.meidusa.amoeba.shutdown;

import java.io.File;

public abstract class ShutdownRunner extends Thread implements Runnable{
	protected File socketInfoFile;
	protected String appplicationName;
	public ShutdownRunner(String appplicationName) {
		this.appplicationName = appplicationName;
	}

	public void init() {
		String home = System.getProperty("project.home", ".");
		socketInfoFile = new File(home, appplicationName + ".shutdownPort");
	}

}
