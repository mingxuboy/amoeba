package com.meidusa.amoeba.mongodb.context;

import com.meidusa.amoeba.context.ProxyRuntimeContext;

public class MongodbProxyRuntimeContext extends ProxyRuntimeContext {
	public MongodbProxyRuntimeContext(){
		ProxyRuntimeContext.setInstance(this);
	}
	@Override
	protected String getDefaultServerConnectionFactoryClassName() {
		return null;
	}

}
