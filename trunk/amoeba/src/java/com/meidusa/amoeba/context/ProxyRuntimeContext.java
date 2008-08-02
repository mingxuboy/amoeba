/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.meidusa.amoeba.net.poolable.MultipleLoadBalanceObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.config.BeanObjectEntityConfig;
import com.meidusa.amoeba.config.ConfigurationException;
import com.meidusa.amoeba.config.DBServerConfig;
import com.meidusa.amoeba.config.DocumentUtil;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.config.ProxyServerConfig;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.route.QueryRouter;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class ProxyRuntimeContext implements Reporter{
	public static final String DEFAULT_SERVER_CONNECTION_MANAGER_CLASS = "com.meidusa.amoeba.net.AuthingableConnectionManager";
	public static final String DEFAULT_REAL_POOL_CLASS = "com.meidusa.amoeba.net.poolable.PoolableObjectPool";
	public static final String DEFAULT_VIRTUAL_POOL_CLASS = "com.meidusa.amoeba.server.MultipleServerPool";
	
	protected static Logger logger = Logger.getLogger(ProxyRuntimeContext.class);
	private static ProxyRuntimeContext context;
	public static ProxyRuntimeContext getInstance(){
		return context;
	}
	
	protected static void setInstance(ProxyRuntimeContext context){
		ProxyRuntimeContext.context = context;
	}
	
	protected abstract String getDefaultServerConnectionFactoryClassName();
	
	protected String getDefaultServerConnectionManagerClassName(){
		return DEFAULT_SERVER_CONNECTION_MANAGER_CLASS;
	}
	
	protected String getDefaultRealPoolClassName(){
		return DEFAULT_REAL_POOL_CLASS;
	}
	
	protected String getDefaultVirtualPoolClassName(){
		return DEFAULT_VIRTUAL_POOL_CLASS;
	}
	
	private Executor readExecutor;
	private Executor clientSideExecutor;
	private Executor serverSideExecutor;
	private Map<String,ConnectionManager> conMgrMap = new HashMap<String,ConnectionManager>();
	private Map<String,ObjectPool> poolMap = new HashMap<String,ObjectPool>();
	
	private Map<String,ConnectionManager> readOnlyConMgrMap = Collections.unmodifiableMap(conMgrMap);
	private Map<String,ObjectPool> readOnlyPoolMap = Collections.unmodifiableMap(poolMap);
	private ProxyServerConfig config ;
	
	private QueryRouter queryRouter;
	
	static class ReNameableThreadExecutor extends ThreadPoolExecutor{
		//Map<Thread,String> threadNameMap = new HashMap<Thread,String>();
		public ReNameableThreadExecutor(int poolSize) {
			super(poolSize, poolSize, Long.MAX_VALUE, TimeUnit.NANOSECONDS,new LinkedBlockingQueue<Runnable>());
		}
		
		/*protected void beforeExecute(Thread t, Runnable r) {
			if(r instanceof NameableRunner){
				NameableRunner nameableRunner = (NameableRunner)r;
				String name = t.getName();
				if(name != null){
					threadNameMap.put(t, t.getName());
					t.setName(nameableRunner.getRunnerName()+":"+t.getName());
				}
			}
		};
		protected void afterExecute(Runnable r, Throwable t) { 
			if(r instanceof NameableRunner){
				String name = threadNameMap.remove(Thread.currentThread());
				if(name != null){
					Thread.currentThread().setName(name);
				}
			}
		};*/
		
	}
	protected ProxyRuntimeContext(){
	}
	
	public Map<String,ConnectionManager> getConnectionManagerList(){
		return readOnlyConMgrMap;
	}
	
	public Executor getClientSideExecutor(){
		return clientSideExecutor;
	}
	
	public Executor getReadExecutor(){
		return readExecutor;
	}
	
	public Executor getServerSideExecutor(){
		return serverSideExecutor;
	}
	
	public Map<String,ObjectPool> getPoolMap(){
		return readOnlyPoolMap;
	}
	
	private List<Initialisable> initialisableList = new ArrayList<Initialisable>();
	public void init(String file){
		
		config = loadConfig(file);
		readExecutor = new ReNameableThreadExecutor(config.getReadThreadPoolSize());
		serverSideExecutor = new ReNameableThreadExecutor(config.getServerSideThreadPoolSize());
		clientSideExecutor = new ReNameableThreadExecutor(config.getClientSideThreadPoolSize());
		
		for(Map.Entry<String, BeanObjectEntityConfig> entry : config.getManagers().entrySet()){
			BeanObjectEntityConfig beanObjectEntityConfig = entry.getValue();
			try {
				ConnectionManager manager = (ConnectionManager) beanObjectEntityConfig.createBeanObject(false);
				manager.setName(entry.getKey());
				if(manager instanceof Initialisable){
					initialisableList.add((Initialisable)manager);
				}
				this.conMgrMap.put(entry.getKey(), manager);
			} catch (Exception e) {
				throw new ConfigurationException("manager instance error",e);
			}
		}
		
		for(Map.Entry<String, DBServerConfig> entry : config.getDbServers().entrySet()){
			DBServerConfig mysqlServerConfig = entry.getValue();
			try {
				BeanObjectEntityConfig poolConfig = mysqlServerConfig.getPoolConfig();
				ObjectPool pool = (ObjectPool)poolConfig.createBeanObject(false);
				if(pool instanceof Initialisable){
					initialisableList.add((Initialisable)pool);
				}
				if(mysqlServerConfig.getFactoryConfig() != null){
					PoolableObjectFactory factory = (PoolableObjectFactory)mysqlServerConfig.getFactoryConfig().createBeanObject(false);
					if(factory instanceof Initialisable){
						initialisableList.add((Initialisable)factory);
					}
					pool.setFactory(factory);
				}
				this.poolMap.put(entry.getKey(), pool);
			} catch (Exception e) {
				throw new ConfigurationException("manager instance error",e);
			}
		}
		
		if(config.getQueryRouterConfig() != null){
			BeanObjectEntityConfig queryRouterConfig = config.getQueryRouterConfig();
			try {
				queryRouter = (QueryRouter)queryRouterConfig.createBeanObject(false);
				if(queryRouter instanceof Initialisable){
					initialisableList.add((Initialisable)queryRouter);
				}
			} catch (Exception e) {
				throw new ConfigurationException("queryRouter instance error",e);
			}
		}
		
		initAllInitialisableBeans();
		initialisableList.clear();
		for(ConnectionManager conMgr :getConnectionManagerList().values()){
			conMgr.setExecutor(this.getReadExecutor());
			conMgr.start();
		}
		
		for(Map.Entry<String, ObjectPool> entry: poolMap.entrySet()){
			ObjectPool pool = entry.getValue();
			if(pool instanceof MultipleLoadBalanceObjectPool){
				MultipleLoadBalanceObjectPool multiPool = (MultipleLoadBalanceObjectPool)pool;
				multiPool.initAllPools();
			}else{
				try{
					PoolableObject object = (PoolableObject)pool.borrowObject();
					pool.returnObject(object);
				}catch(Exception e){
				}
			}
		}
	}
	
	
	private void initAllInitialisableBeans(){
		for(Initialisable bean : initialisableList){
			try {
				bean.init();
			} catch (InitialisationException e) {
				throw new ConfigurationException("Initialisation Exception",e);
			}
		}
	}
	
	private ProxyServerConfig loadConfig(String configFileName){
			DocumentBuilder db;

	        try {
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            dbf.setValidating(true);
	            dbf.setNamespaceAware(false);

	            db = dbf.newDocumentBuilder();
	            db.setEntityResolver(new EntityResolver() {
	                public InputSource resolveEntity(String publicId, String systemId) {
	                	if (systemId.endsWith("amoeba.dtd")) {
	                	      InputStream in = ProxyRuntimeContext.class.getResourceAsStream("/com/meidusa/amoeba/xml/amoeba.dtd");
	                	      if (in == null) {
	                		LogLog.error("Could not find [amoeba.dtd]. Used [" + ProxyRuntimeContext.class.getClassLoader() 
	                			     + "] class loader in the search.");
	                		return null;
	                	      } else {
	                		return new InputSource(in);
	                	      }
	            	    } else {
	            	      return null;
	            	    }
	                }
	            });
	            
	            db.setErrorHandler(new ErrorHandler() {
	                public void warning(SAXParseException exception) {
	                }

	                public void error(SAXParseException exception) throws SAXException {
	                    logger.error(exception.getMessage() + " at (" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")");
	                    throw exception;
	                }

	                public void fatalError(SAXParseException exception) throws SAXException {
	                    logger.fatal(exception.getMessage() + " at (" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")");
	                    throw exception;
	                }
	            });
	           return loadConfigurationFile(configFileName, db);
	        } catch (Exception e) {
	            logger.fatal("Could not load configuration file, failing", e);
	            throw new ConfigurationException("Error loading configuration file " + configFileName, e);
	        }
	}
	
	 private ProxyServerConfig loadConfigurationFile(String fileName, DocumentBuilder db) {
            Document doc = null;
            InputStream is = null;
            ProxyServerConfig config = new ProxyServerConfig();
            try {
                is = new FileInputStream(new File(fileName));

                if (is == null) {
                    throw new Exception("Could not open file " + fileName);
                }

                doc = db.parse(is);
            } catch (Exception e) {
                final String s = "Caught exception while loading file " + fileName;
                logger.error(s, e);
                throw new ConfigurationException(s, e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        logger.error("Unable to close input stream", e);
                    }
                }
            }
            Element rootElement = doc.getDocumentElement();
            NodeList children = rootElement.getChildNodes();
            int childSize = children.getLength();

            for (int i = 0; i < childSize; i++) {
                Node childNode = children.item(i);

                if (childNode instanceof Element) {
                    Element child = (Element) childNode;

                    final String nodeName = child.getNodeName();
                    if (nodeName.equals("server")) {
                        loadServerConfig(child,config);
                    }else if (nodeName.equals("connectionManagerList")) {
                    	loadConnectionManagers(child,config);
                    }else if (nodeName.equals("dbServerList")) {
                        loadServers(child,config);
                    }else if (nodeName.equals("queryRouter")) {
                    	loadQueryRouter(rootElement,config);
                    }
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("Loaded Engine configuration from: " + fileName);
            }
            return config;
	}

	private void loadQueryRouter(Element current,ProxyServerConfig config){
		BeanObjectEntityConfig queryRouter = DocumentUtil.loadBeanConfig(DocumentUtil.getTheOnlyElement(current,"queryRouter"));
		config.setQueryRouterConfig(queryRouter);
	}
	
	private void loadServers(Element current , ProxyServerConfig config) {
		NodeList children = current.getChildNodes();
        int childSize = children.getLength();
        for (int i = 0; i < childSize; i++) {
        	Node childNode = children.item(i);
        	if (childNode instanceof Element) {
        		 Element child = (Element) childNode;
        		 DBServerConfig serverConfig = loadServer(child);
        		 if(serverConfig.isVirtual()){
        			 if(serverConfig.getPoolConfig() != null){
        				 if(StringUtil.isEmpty( serverConfig.getPoolConfig().getClassName())){
        					 serverConfig.getPoolConfig().setClassName(getDefaultVirtualPoolClassName());
        				 }
        			 }
        		 }else{
        			 if(serverConfig.getPoolConfig() != null){
        				 if(StringUtil.isEmpty( serverConfig.getPoolConfig().getClassName())){
        					 serverConfig.getPoolConfig().setClassName(getDefaultRealPoolClassName());
        				 }
        			 }
        		 }
        		 
        		 if(serverConfig.getFactoryConfig() != null){
    				 if(StringUtil.isEmpty(serverConfig.getFactoryConfig().getClassName())){
    					 serverConfig.getFactoryConfig().setClassName(getDefaultServerConnectionFactoryClassName());
    				 }
    			 }
        		 config.addServer(serverConfig.getName(), serverConfig);
        	}
        }
	}

	private DBServerConfig loadServer(Element current){
		DBServerConfig serverConfig = new DBServerConfig();
		NamedNodeMap nodeMap = current.getAttributes();
		Map<String,String> map = new HashMap<String,String>();
		for(int i=0;i<nodeMap.getLength();i++){
			Node node = nodeMap.item(i);
			if (node instanceof org.w3c.dom.Attr) {
				Attr attr =(Attr)node;
				map.put(attr.getName(),attr.getNodeValue());
			}
		}
		ParameterMapping.mappingObject(serverConfig, map);
		
		BeanObjectEntityConfig factory = DocumentUtil.loadBeanConfig(DocumentUtil.getTheOnlyElement(current,"factoryConfig"));
		BeanObjectEntityConfig pool = DocumentUtil.loadBeanConfig(DocumentUtil.getTheOnlyElement(current,"poolConfig"));
		serverConfig.setPoolConfig(pool);
		serverConfig.setFactoryConfig(factory);
		
		return serverConfig;
	}
	
	private void loadConnectionManagers(Element current, ProxyServerConfig config) {
		NodeList children = current.getChildNodes();
        int childSize = children.getLength();
        for (int i = 0; i < childSize; i++) {
        	Node childNode = children.item(i);
        	if (childNode instanceof Element) {
        		 Element child = (Element) childNode;
        		 BeanObjectEntityConfig managerConfig = DocumentUtil.loadBeanConfig(child);
        		 if(StringUtil.isEmpty(managerConfig.getClassName())){
        			 managerConfig.setClassName(getDefaultServerConnectionManagerClassName());
        		 }
        		 config.addManager(managerConfig.getName(), managerConfig);
        	}
        }
	}

	private void loadServerConfig(Element current,ProxyServerConfig config) {
		NodeList children = current.getChildNodes();
        int childSize = children.getLength();
        Map<String,String> map = new HashMap<String,String>();
        for (int i = 0; i < childSize; i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element child = (Element) childNode;
                final String nodeName = child.getNodeName();
	            if (nodeName.equals("property")) {
	            	String key = child.getAttribute("name");
	            	String value = child.getTextContent();
	            	map.put(key, value);
	            }
            }
        }
        ParameterMapping.mappingObject(config, map);
	}

	
	
	
	public void appendReport(StringBuilder buffer, long now, long sinceLast,
			boolean reset,Level level) {
		for(Map.Entry<String, ObjectPool> entry :getPoolMap().entrySet()){
			ObjectPool pool = entry.getValue();
			String poolName = entry.getKey();
			buffer.append("* Server pool=").append(poolName == null?"default pool":poolName).append("\n")
			.append(" - pool active Size=").append(pool.getNumActive());
			buffer.append(", pool Idle size=").append(pool.getNumIdle()).append("\n");
		}
	}

	public ProxyServerConfig getConfig() {
		return config;
	}

	public QueryRouter getQueryRouter() {
		return queryRouter;
	}
}
