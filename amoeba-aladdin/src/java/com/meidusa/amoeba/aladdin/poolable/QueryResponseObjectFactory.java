package com.meidusa.amoeba.aladdin.poolable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool.PoolableObjectFactory;

import com.meidusa.amoeba.aladdin.handler.MessageHandlerRunner;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;

public class QueryResponseObjectFactory implements PoolableObjectFactory,Initialisable{
	private Class<MessageHandlerRunner> messageHandlerRunner;
	private Map<String,Object> runnerParameters = new HashMap<String,Object>();
	
	public Map<String, Object> getRunnerParameters() {
		return runnerParameters;
	}

	public void setRunnerParameters(Map<String, Object> parameters) {
		this.runnerParameters = parameters;
	}

	public void setMessageHandlerRunner(Class<MessageHandlerRunner> messageHandlerRunner) {
		this.messageHandlerRunner = messageHandlerRunner;
	}

	public void activateObject(Object obj) throws Exception {
		
	}

	public void destroyObject(Object obj) throws Exception {
		
	}

	public Object makeObject() throws Exception {
		QueryResponse object = new QueryResponse();
		MessageHandlerRunner runner = messageHandlerRunner.newInstance();
		
		ParameterMapping.mappingObject(runner, runnerParameters,null);
		
		if(runner instanceof Initialisable){
			((Initialisable)runner).init();
		}
		
		object.setMessageHandlerRunner(runner);
		if(object instanceof Initialisable){
			((Initialisable)object).init();
		}
		return object;
	}

	public void passivateObject(Object obj) throws Exception {
		QueryResponse object = (QueryResponse)obj;
		object.getRunner().reset();
	}

	public boolean validateObject(Object obj) {
		return true;
	}

	public void init() throws InitialisationException {
		
	}

}
