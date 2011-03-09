/**
 * <pre>
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
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
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.meidusa.amoeba.config.BeanObjectEntityConfig;
import com.meidusa.amoeba.config.ConfigurationException;
import com.meidusa.amoeba.config.DBServerConfig;
import com.meidusa.amoeba.config.DocumentUtil;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.config.ProxyServerConfig;
import com.meidusa.amoeba.heartbeat.HeartbeatDelayed;
import com.meidusa.amoeba.heartbeat.HeartbeatManager;
import com.meidusa.amoeba.heartbeat.Status;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.net.poolable.MultipleLoadBalanceObjectPool;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.route.QueryRouter;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.StringUtil;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class ProxyRuntimeContext implements Reporter {

    public static final String             DEFAULT_SERVER_CONNECTION_MANAGER_CLASS = "com.meidusa.amoeba.net.AuthingableConnectionManager";
    public static final String             DEFAULT_REAL_POOL_CLASS                 = "com.meidusa.amoeba.net.poolable.PoolableObjectPool";
    public static final String             DEFAULT_VIRTUAL_POOL_CLASS              = "com.meidusa.amoeba.server.MultipleServerPool";

    protected static Logger                logger                                  = Logger.getLogger(ProxyRuntimeContext.class);

    private static ProxyRuntimeContext     context = null;

    private ProxyServerConfig              config;

    private Map<String, ConnectionManager> conMgrMap                               = new HashMap<String, ConnectionManager>();
    private Map<String, ConnectionManager> readOnlyConMgrMap                       = Collections.unmodifiableMap(conMgrMap);

    private Map<String, ObjectPool>        poolMap                                 = new HashMap<String, ObjectPool>();
    private Map<String, ObjectPool>        readOnlyPoolMap                         = Collections.unmodifiableMap(poolMap);

    private List<ContextChangedListener> listeners = new ArrayList<ContextChangedListener>();
    
    @SuppressWarnings("unchecked")
	private QueryRouter                    queryRouter;
    private RuntimeContext runtimeContext;
    
    private Map<String, Object> beanContext = new HashMap<String, Object>();
    
	public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

	public static ProxyRuntimeContext getInstance() {
        return context;
    }

    public static void setInstance(ProxyRuntimeContext context) {
        ProxyRuntimeContext.context = context;
    }

    protected String getDefaultServerConnectionManagerClassName() {
        return DEFAULT_SERVER_CONNECTION_MANAGER_CLASS;
    }

    protected String getDefaultRealPoolClassName() {
        return DEFAULT_REAL_POOL_CLASS;
    }

    protected String getDefaultVirtualPoolClassName() {
        return DEFAULT_VIRTUAL_POOL_CLASS;
    }

    public ProxyServerConfig getConfig() {
        return config;
    }

    public QueryRouter getQueryRouter() {
        return queryRouter;
    }

    public ProxyRuntimeContext(){
    }

    public Map<String, ConnectionManager> getConnectionManagerList() {
        return readOnlyConMgrMap;
    }

    public Map<String, ObjectPool> getPoolMap() {
        return readOnlyPoolMap;
    }

    private List<Initialisable> initialisableList = new ArrayList<Initialisable>();

    /**
     * 
     * @param parent
     * @param dest
     * @return
     */
    protected void inheritDBServerConfig(DBServerConfig parent ,DBServerConfig dest){
    	BeanObjectEntityConfig destBeanConfig = dest.getFactoryConfig();
		BeanObjectEntityConfig parentBeanConfig = parent.getFactoryConfig();
		
    	if(destBeanConfig != null){
    		if(parentBeanConfig != null){
    			inheritBeanObjectEntityConfig(parentBeanConfig,destBeanConfig);
    		}
    	}else{
    		dest.setFactoryConfig(parentBeanConfig);
    	}
    	
    	destBeanConfig = dest.getPoolConfig();
		parentBeanConfig = parent.getPoolConfig();
		
		if(destBeanConfig != null){
    		if(parentBeanConfig != null){
    			inheritBeanObjectEntityConfig(parentBeanConfig,destBeanConfig);
    		}
    	}else{
    		dest.setPoolConfig(parentBeanConfig);
    	}
		
		if(dest.getVirtual()== null){
			dest.setVirtual(parent.getVirtual());
		}
		
		if(dest.getAbstractive()== null){
			dest.setAbstractive(parent.getAbstractive());
		}
		
    }
    
    public void addContextChangedListener(ContextChangedListener listener){
    	if(!listeners.contains(listener)){
    		this.listeners.add(listener);
    	}
    }
    
    public void removeContextChangedListener(ContextChangedListener listener){
		this.listeners.remove(listener);
    }
    
    public void notifyAllContextChangedListener(){
    	for(ContextChangedListener listener : listeners){
    		listener.doChange();
    	}
    }
    
    
    protected  void inheritBeanObjectEntityConfig(BeanObjectEntityConfig parent,BeanObjectEntityConfig dest){
    	BeanObjectEntityConfig parentCloned = (BeanObjectEntityConfig)parent.clone();
    	if(!StringUtil.isEmpty(dest.getClassName())){
    		parentCloned.setClassName(dest.getClassName());
    	}
    	
    	/*if(!StringUtil.isEmpty(dest.getName())){
    		parentCloned.setName(dest.getName());
    	}*/
    	
    	if(dest.getParams() != null){
    		if(parentCloned.getParams() == null){
    			parentCloned.setParams(dest.getParams());
    		}else{
    			parentCloned.getParams().putAll(dest.getParams());
    		}
    	}
    	
    	dest.setClassName(parentCloned.getClassName());
    	//dest.setName(parentCloned.getName());
    	dest.setParams(parentCloned.getParams());
    }
    
    
    private Object createBeanObjectEntity(BeanObjectEntityConfig config,boolean initEarly){
    	Object object = config.createBeanObject(initEarly);
    	if(!StringUtil.isEmpty(config.getName())){
    		beanContext.put(config.getName(), object);
    	}
    	return object; 
    }
    
    public void init(String file) {
        config = loadConfig(file);
        this.runtimeContext = (RuntimeContext)createBeanObjectEntity(config.getRuntimeConfig(),true);
        /*for (Map.Entry<String, BeanObjectEntityConfig> entry : config.getManagers().entrySet()) {
            BeanObjectEntityConfig beanObjectEntityConfig = entry.getValue();
            try {
                ConnectionManager manager = (ConnectionManager) beanObjectEntityConfig.createBeanObject(false);
                manager.setName(entry.getKey());
                initialisableList.add(manager);
                conMgrMap.put(manager.getName(), manager);
            } catch (Exception e) {
                throw new ConfigurationException("manager instance error", e);
            }
        }*/

        for (Map.Entry<String, DBServerConfig> entry : config.getDbServers().entrySet()) {
            DBServerConfig dbServerConfig = (DBServerConfig)entry.getValue().clone();
            String parent = dbServerConfig.getParent();
            if(!StringUtil.isEmpty(parent)){
            	DBServerConfig parentConfig = config.getDbServers().get(parent);
            	if(parentConfig == null || parentConfig.getParent() != null){
            		throw new ConfigurationException(entry.getKey()+" cannot found parent with name="+parent+" or parent's parent must be null");
            	}
            	inheritDBServerConfig(parentConfig,dbServerConfig);
            }
            
            //ignore if dbServer is abstract
            if(dbServerConfig.getAbstractive() != null && dbServerConfig.getAbstractive().booleanValue()){
            	continue;
            }
            
            try {
                BeanObjectEntityConfig poolConfig = dbServerConfig.getPoolConfig();
                ObjectPool pool = (ObjectPool) poolConfig.createBeanObject(false,conMgrMap);
                pool.setName(StringUtil.isEmpty(poolConfig.getName())?dbServerConfig.getName():poolConfig.getName());
                if (pool instanceof Initialisable) {
                    initialisableList.add((Initialisable) pool);
                }
                if (dbServerConfig.getFactoryConfig() != null) {
                    PoolableObjectFactory factory = (PoolableObjectFactory) dbServerConfig.getFactoryConfig().createBeanObject(false,conMgrMap);
                    if (factory instanceof Initialisable) {
                        initialisableList.add((Initialisable) factory);
                    }
                    pool.setFactory(factory);
                }
                poolMap.put(entry.getKey(), pool);
            } catch (Exception e) {
                throw new ConfigurationException("createBean error dbServer="+dbServerConfig.getName(), e);
            }
        }

        if (config.getQueryRouterConfig() != null) {
            BeanObjectEntityConfig queryRouterConfig = config.getQueryRouterConfig();
            try {
                queryRouter = (QueryRouter) queryRouterConfig.createBeanObject(false,conMgrMap);
                if (queryRouter instanceof Initialisable) {
                    initialisableList.add((Initialisable) queryRouter);
                }
                if(queryRouter instanceof ContextChangedListener){
                	this.addContextChangedListener((ContextChangedListener)queryRouter);
                }
            } catch (Exception e) {
                throw new ConfigurationException("queryRouter instance error", e);
            }
        }

        initAllInitialisableBeans();
        initialisableList.clear();
        for (ConnectionManager cm : getConnectionManagerList().values()) {
            cm.start();
        }
        initPools();
    }

    protected void initPools() {
        for (Map.Entry<String, ObjectPool> entry : poolMap.entrySet()) {
            ObjectPool pool = entry.getValue();
            if (pool instanceof MultipleLoadBalanceObjectPool) {
                MultipleLoadBalanceObjectPool multiPool = (MultipleLoadBalanceObjectPool) pool;
                multiPool.initAllPools();
            } else {
                PoolableObject object = null;
                try {
                    object = (PoolableObject) pool.borrowObject();
                } catch (Exception e) {
                    logger.error("init pool error!", e);
                } finally {
                    if (object != null) {
                        try {
                            pool.returnObject(object);
                        } catch (Exception e) {
                            logger.error("return init pools error", e);
                        }
                    }
                }
            }
            
            if(pool instanceof ContextChangedListener){
            	this.addContextChangedListener((ContextChangedListener)pool);
            }
        }
    }

    private void initAllInitialisableBeans() {
        for (Initialisable bean : initialisableList) {
            try {
                bean.init();
                if(bean instanceof ContextChangedListener){
                	this.addContextChangedListener((ContextChangedListener)bean);
                }
            } catch (InitialisationException e) {
                throw new ConfigurationException("Initialisation bean="+bean+" error", e);
            }
        }
    }

    private ProxyServerConfig loadConfig(String configFileName) {
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
                            LogLog.error("Could not find [amoeba.dtd]. Used [" + ProxyRuntimeContext.class.getClassLoader() + "] class loader in the search.");
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
                if (nodeName.equals("proxy")) {
                	loadProxyConfig(child, config);
                } else if (nodeName.equals("connectionManagerList")) {
                    loadConnectionManagers(child, config);
                } else if (nodeName.equals("dbServerLoader")) {
                    loadDbServerLoader(rootElement, config);
                } else if (nodeName.equals("queryRouter")) {
                    loadQueryRouter(rootElement, config);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Loaded Amoeba Proxy configuration from: " + fileName);
        }
        return config;
    }

    private void loadQueryRouter(Element current, ProxyServerConfig config) {
        BeanObjectEntityConfig queryRouter = DocumentUtil.loadBeanConfig(DocumentUtil.getTheOnlyElement(current, "queryRouter"));
        config.setQueryRouterConfig(queryRouter);
    }

    private void loadDbServerLoader(Element current, ProxyServerConfig config) {
    	BeanObjectEntityConfig dbserverLoader = DocumentUtil.loadBeanConfig(DocumentUtil.getTheOnlyElement(current, "dbServerLoader"));
    	DBServerConfigLoader loader = (DBServerConfigLoader)dbserverLoader.createBeanObject(true, this.getConnectionManagerList());
    	config.putAllServers(loader.loadConfig());
    }

    private void loadConnectionManagers(Element current, ProxyServerConfig config) {
        NodeList children = current.getChildNodes();
        int childSize = children.getLength();
        for (int i = 0; i < childSize; i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element child = (Element) childNode;
                BeanObjectEntityConfig managerConfig = DocumentUtil.loadBeanConfig(child);
                if (StringUtil.isEmpty(managerConfig.getClassName())) {
                    managerConfig.setClassName(getDefaultServerConnectionManagerClassName());
                }
                config.addManager(managerConfig.getName(), managerConfig);
            }
        }
        
        //create bean and init
        for (Map.Entry<String, BeanObjectEntityConfig> entry : config.getManagers().entrySet()) {
            BeanObjectEntityConfig beanObjectEntityConfig = entry.getValue();
            try {
                ConnectionManager manager = (ConnectionManager) createBeanObjectEntity(beanObjectEntityConfig,true);
                manager.setName(entry.getKey());
                conMgrMap.put(manager.getName(), manager);
            } catch (Exception e) {
                throw new ConfigurationException("manager instance error", e);
            }
        }
    }

    private void loadProxyConfig(Element current, ProxyServerConfig config) {
        NodeList children = current.getChildNodes();
        int childSize = children.getLength();
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < childSize; i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                Element child = (Element) childNode;
                final String nodeName = child.getNodeName();
                if (nodeName.equals("property")) {
                    String key = child.getAttribute("name");
                    String value = child.getTextContent();
                    map.put(key, value);
                }else if(nodeName.equals("service")){
                	BeanObjectEntityConfig server = DocumentUtil.loadBeanConfig(child);
                	config.addServerConfig(server);
                }else if(nodeName.equals("runtime")){
                	BeanObjectEntityConfig runtime = DocumentUtil.loadBeanConfig(child);
                	config.setRuntimeConfig(runtime);
                }
            }
        }
        ParameterMapping.mappingObject(config, map,null);
    }

    public void appendReport(StringBuilder buffer, long now, long sinceLast, boolean reset, Level level) {
        for (Map.Entry<String, ObjectPool> entry : getPoolMap().entrySet()) {
            ObjectPool pool = entry.getValue();
            String poolName = entry.getKey();
            buffer.append("* Server pool=").append(poolName == null ? "default pool" : poolName).append("\n").append(" - pool active Size=").append(pool.getNumActive());
            buffer.append(", pool Idle size=").append(pool.getNumIdle()).append("\n");
        }
    }

    
    static class CloseObjectPoolHeartbeatDelayed extends HeartbeatDelayed{
		private ObjectPool pool;
		public CloseObjectPoolHeartbeatDelayed(long nsTime, TimeUnit timeUnit,ObjectPool pool) {
			super(nsTime, timeUnit);
			this.pool = pool;
		}

		@Override
		public Status doCheck() {
			if(pool.getNumActive() == 0){
				return Status.VALID;
			}
			return null;
		}

		public boolean isCycle(){
        	return false;
        }
		
		public void cancel(){
        	try {
				this.pool.close();
			} catch (Exception e) {
			}
        }
		
		public boolean equals(Object obj) {
	    	if(obj instanceof CloseObjectPoolHeartbeatDelayed){
	    		CloseObjectPoolHeartbeatDelayed other = (CloseObjectPoolHeartbeatDelayed)obj;
	    		return other.pool == this.pool && this.getClass() == obj.getClass();
	    	}else{
	    		return false;
	    	}
        }
	    
	    public int hashCode(){
	    	return pool == null?this.getClass().hashCode():this.getClass().hashCode() + pool.hashCode();
	    }

		@Override
		public String getName() {
			return "closing Pool="+pool.getName();
		}
		
	}
	
	private ObjectPool createObjectPool(DBServerConfig config) throws ConfigurationException{
		ObjectPool pool = null;
		try {
            BeanObjectEntityConfig poolConfig = config.getPoolConfig();
            pool = (ObjectPool) createBeanObjectEntity(poolConfig,true);
            pool.setName(StringUtil.isEmpty(poolConfig.getName())?config.getName():poolConfig.getName());
            
            if (config.getFactoryConfig() != null) {
                PoolableObjectFactory factory = (PoolableObjectFactory) createBeanObjectEntity(config.getFactoryConfig(),true);
                pool.setFactory(factory);
            }
        } catch (Exception e) {
            throw new ConfigurationException("createBean error", e);
        }
        
        if (pool instanceof MultipleLoadBalanceObjectPool) {
            MultipleLoadBalanceObjectPool multiPool = (MultipleLoadBalanceObjectPool) pool;
            multiPool.initAllPools();
        } else {
            PoolableObject object = null;
            try {
                object = (PoolableObject) pool.borrowObject();
            } catch (Exception e) {
                logger.error("init pool error!", e);
                throw new ConfigurationException("init pool error!", e);
            } finally {
                if (object != null) {
                    try {
                        pool.returnObject(object);
                    } catch (Exception e) {
                        logger.error("return init pools error", e);
                        throw new ConfigurationException("return init pools error", e);
                    }
                }
            }
        }
        
        return pool;
	}
	
	/**
	 * update dbServerConfig
	 * @param sourceConfig
	 * @param tryUpdate
	 * @throws ConfigurationException
	 */
	public void updateDBServer(DBServerConfig sourceConfig,boolean tryUpdate) throws ConfigurationException {
		boolean abstractive = sourceConfig.getAbstractive();
		
		if (sourceConfig == null || StringUtil.isEmpty(sourceConfig.getName())) {
			throw new ConfigurationException("config or config's name cannot be null");
		}

		if(tryUpdate){
		
			//try to create ObjectPool with this sourceConfig
			if(!abstractive){
				DBServerConfig config = (DBServerConfig)sourceConfig.clone();
				if (sourceConfig.getParent() != null) {
					DBServerConfig parent = this.getConfig().getDbServers().get(config.getParent());
					if (parent == null) {
						throw new ConfigurationException("parent config withe name=" + config.getParent() + " not found");
					}
					this.inheritDBServerConfig(parent, config);
				}
				
				ObjectPool pool = createObjectPool(config);
				try {
					pool.close();
				} catch (Exception e) {
					
				}
			}else{
				Map<String,DBServerConfig> dbServerConfigs = this.getConfig().getDbServers();
				for(Map.Entry<String,DBServerConfig> entry : dbServerConfigs.entrySet()){
					if(StringUtil.equals(entry.getValue().getParent(),sourceConfig.getName())){
						if(!entry.getValue().getAbstractive()){
							DBServerConfig child = (DBServerConfig)entry.getValue().clone();
							this.inheritDBServerConfig(sourceConfig, child);
							ObjectPool pool = createObjectPool(child);
							try {
								pool.close();
							} catch (Exception e) {
								
							}
							break;
						}
					}
				}
			}
			
		}else{
		
			/**
			 * close old objectPool
			 * if this configuration is abstractive then close all children's objectPools 
			 * else the old ObjectPool will be closed
			 * 
			 */
			this.getConfig().addServer(sourceConfig.getName(),sourceConfig);
			
			if (!abstractive) {
				DBServerConfig config = (DBServerConfig)sourceConfig.clone();
				if (sourceConfig.getParent() != null) {
					DBServerConfig parent = this.getConfig().getDbServers().get(sourceConfig.getParent());
					if (parent == null) {
						throw new ConfigurationException("parent config withe name=" + sourceConfig.getParent() + " not found");
					}
	
					this.inheritDBServerConfig(parent, config);
				}
				
				ObjectPool pool = createObjectPool(config);
				
				
				//close old ObjectPool
				ObjectPool oldObjectPool = this.getPoolMap().get(config.getName());
				
				this.getPoolMap().put(config.getName(), pool);
				this.notifyAllContextChangedListener();
				if(oldObjectPool != null){
					
					//MultipleLoadBalanceObjectPool not to be closed;
					if(!(oldObjectPool instanceof MultipleLoadBalanceObjectPool)){
						CloseObjectPoolHeartbeatDelayed delay = new CloseObjectPoolHeartbeatDelayed(5,TimeUnit.SECONDS,oldObjectPool);
						HeartbeatManager.addHeartbeat(delay);
					}
				}
			}else{
				//close all children's ObjectPools
				Map<String,DBServerConfig> dbServerConfigs = this.getConfig().getDbServers();
				for(Map.Entry<String,DBServerConfig> entry : dbServerConfigs.entrySet()){
					if(StringUtil.equals(entry.getValue().getParent(),sourceConfig.getName())){
						if(!entry.getValue().getAbstractive()){
							DBServerConfig child = (DBServerConfig)entry.getValue().clone();
							this.inheritDBServerConfig(sourceConfig, child);
							
							ObjectPool pool = createObjectPool(child);
							
							//close old ObjectPool
							ObjectPool oldObjectPool = this.getPoolMap().get(child.getName());
							
							this.getPoolMap().put(child.getName(), pool);
							this.notifyAllContextChangedListener();
							if(oldObjectPool != null){
								
								//MultipleLoadBalanceObjectPool not to be closed;
								if(!(oldObjectPool instanceof MultipleLoadBalanceObjectPool)){
									CloseObjectPoolHeartbeatDelayed delay = new CloseObjectPoolHeartbeatDelayed(5,TimeUnit.SECONDS,oldObjectPool);
									HeartbeatManager.addHeartbeat(delay);
								}
							}
						}
					}
				}
				
				this.getConfig().addServer(sourceConfig.getName(),sourceConfig);
			}
		}
	}
}
