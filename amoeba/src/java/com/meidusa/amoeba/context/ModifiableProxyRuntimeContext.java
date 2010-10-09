package com.meidusa.amoeba.context;

import java.util.Map;

import org.apache.commons.pool.PoolableObjectFactory;

import com.meidusa.amoeba.config.BeanObjectEntityConfig;
import com.meidusa.amoeba.config.ConfigurationException;
import com.meidusa.amoeba.config.DBServerConfig;
import com.meidusa.amoeba.net.poolable.MultipleLoadBalanceObjectPool;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.util.StringUtil;

public abstract class ModifiableProxyRuntimeContext extends ProxyRuntimeContext {
	
	public void commitUpdateDBServer(DBServerConfig config){
		
	}
	
	private ObjectPool createObjectPool(DBServerConfig config) throws ConfigurationException{
		ObjectPool pool = null;
		try {
            BeanObjectEntityConfig poolConfig = config.getPoolConfig();
            pool = (ObjectPool) poolConfig.createBeanObject(true);
            pool.setName(StringUtil.isEmpty(poolConfig.getName())?config.getName():poolConfig.getName());
            
            if (config.getFactoryConfig() != null) {
                PoolableObjectFactory factory = (PoolableObjectFactory) config.getFactoryConfig().createBeanObject(true);
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
	
	public void tryUpdateDBServer(DBServerConfig config) throws ConfigurationException {
		boolean abstractive = config.getAbstractive();
		
		if (config == null || StringUtil.isEmpty(config.getName())) {
			throw new ConfigurationException("config or config's name cannot be null");
		}

		if (config.getParent() != null) {
			DBServerConfig parent = this.getConfig().getDbServers().get(config.getParent());
			if (parent == null) {
				throw new ConfigurationException("parent config withe name=" + config.getParent() + " not found");
			}

			this.inheritDBServerConfig(parent, config);
		}
		
		//check can create ObjectPool with this config
		if(!abstractive){
			ObjectPool pool = createObjectPool(config);
		}else{
			Map<String,DBServerConfig> dbServerConfigs = this.getConfig().getDbServers();
			for(Map.Entry<String,DBServerConfig> entry : dbServerConfigs.entrySet()){
				if(StringUtil.equals(entry.getValue().getParent(),config.getName())){
					if(!entry.getValue().getAbstractive()){
						DBServerConfig child = (DBServerConfig)entry.getValue().clone();
						this.inheritDBServerConfig(config, child);
						createObjectPool(entry.getValue());
					}
				}
			}
		}
		
		/**
		 * close old objectPool
		 * if this configuration is abstractive then close all children's objectPools 
		 * else only the old ObjectPool with the same name will be closed
		 * 
		 */
		if (!abstractive) {

			//close old ObjectPool
			ObjectPool oldObjectPool = this.getPoolMap().get(config.getName());
			
			
			
			if (oldObjectPool != null) {
				try {
					oldObjectPool.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			//close all children's ObjectPools
			
		}
	}
}
