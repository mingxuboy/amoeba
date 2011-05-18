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
package com.meidusa.amoeba.route;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.config.BeanObjectEntityConfig;
import com.meidusa.amoeba.config.ConfigurationException;
import com.meidusa.amoeba.context.ContextChangedListener;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.ParseException;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.parser.function.Function;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.sqljep.function.Comparative;
import com.meidusa.amoeba.sqljep.function.ComparativeBaseList;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.StringUtil;
import com.meidusa.amoeba.util.ThreadLocalMap;
import com.meidusa.amoeba.util.Tuple;

/**
 * @author struct
 */
public abstract class  AbstractQueryRouter<T extends Connection,V> implements QueryRouter<T,V>, Initialisable ,ContextChangedListener {
	public static final String _CURRENT_QUERY_OBJECT_ = "_CURRENT_QUERY_OBJECT_";
	protected static Logger logger = Logger.getLogger(AbstractQueryRouter.class);
	private Map<String,Pattern> patternMap = new HashMap<String,Pattern>();
	
    /* 默认1000 */
    private int                                     LRUMapSize      = 1000;
    protected LRUMap                                  map;
    protected Lock                                    mapLock         = new ReentrantLock(false);

    private Map<Table, TableRule>                   tableRuleMap    = new HashMap<Table, TableRule>();
    private Map<Table, TableRule>                   regexTableRuleMap    = new HashMap<Table, TableRule>();
    protected Map<String, Function>                 functionMap     = new HashMap<String, Function>();

    protected ObjectPool[]                          defaultPools;
    protected ObjectPool[]                          readPools;
    protected ObjectPool[]                          writePools;
    protected Tuple<Statement,ObjectPool[]> tuple;
    private File                                  	sqlFunctionFile;

    private String                                  defaultPool;
    private String                                  readPool;
    private String                                  writePool;

    private boolean                                 needParse       = true;
    private TableRuleLoader ruleLoader;
    
    public TableRuleLoader getRuleLoader() {
		return ruleLoader;
	}

	public void setRuleLoader(TableRuleLoader ruleLoader) {
		this.ruleLoader = ruleLoader;
	}

	public AbstractQueryRouter(){
    }

    public void setReadPool(String readPool) {
        this.readPool = readPool;
    }

    public String getReadPool() {
        return readPool;
    }

    public String getWritePool() {
        return writePool;
    }

    public void setWritePool(String writePool) {
        this.writePool = writePool;
    }

    public ObjectPool[] doRoute(T connection,V queryObject) throws ParseException {
        if (queryObject == null) {
            return defaultPools;
        }
        if (needParse) {
            return selectPool(connection, queryObject);
        } else {
            return defaultPools;
        }
    }

    protected abstract  Map<Table, Map<Column, Comparative>> evaluateTable(T connection,V queryObject);
    
    /**
     * 返回Query 被route到目标地址 ObjectPool集合 如果返回null，则是属于DatabaseConnection 自身属性设置的请求。
     * @throws ParseException 
     */
    
    protected void beforeSelectPool(T connection, V queryObject){
    	ThreadLocalMap.put(_CURRENT_QUERY_OBJECT_, queryObject);
    }
    
    protected List<String> evaluate(StringBuffer loggerBuffer,T connection, V queryObject){
    	boolean isRead = true;
		boolean isPrepared = false;
		if (queryObject instanceof Request) {
 			isRead = ((Request) queryObject).isRead();
 			isPrepared = ((Request) queryObject).isPrepared();
 		}
		List<String> poolNames = new ArrayList<String>();
   	 	Map<Table, Map<Column, Comparative>> tables  = evaluateTable(connection,queryObject);

        if (tables != null && tables.size() > 0) {
            Set<Map.Entry<Table, Map<Column, Comparative>>> entrySet = tables.entrySet();
            for (Map.Entry<Table, Map<Column, Comparative>> entry : entrySet) {
            	boolean regexMatched = false;
                Map<Column, Comparative> columnMap = entry.getValue();
                
                TableRule tableRule = this.tableRuleMap.get(entry.getKey());
                
                Table table = entry.getKey();
                
                if(tableRule == null && table.getName() != null){
                	
                	/**
                	 * foreach regex table rule
                	 */
                	for(Map.Entry<Table, TableRule> ruleEntry:this.regexTableRuleMap.entrySet()){
                		Table ruleTable = ruleEntry.getKey();
                		boolean tableMatched = false;
                		boolean schemaMatched = false;
            			
                		/**
                		 * check table name matched or not.
                		 */
                		Pattern pattern = this.getPattern(ruleTable.getName());
            			java.util.regex.Matcher matcher = pattern.matcher(table.getName());
            			if(matcher.find()){
            				tableMatched = true;
            			}
            			
            			/**
                		 * check table schema matched or not.
                		 */
            			pattern = this.getPattern(ruleTable.getSchema().getName());
            			matcher = pattern.matcher(table.getSchema().getName());
            			if(matcher.find()){
            				schemaMatched = true;
            			}
                		
                		if(tableMatched && schemaMatched){
                			tableRule = ruleEntry.getValue();
                			regexMatched = true;
                			break;
                		}
                	}
                }
                
                // 如果存在table Rule 则需要看是否有Rule
                if (tableRule != null) {
                    // 没有列的sql语句，使用默认的tableRule
                    if (columnMap == null || isPrepared) {
                        String[] pools = (isRead ? tableRule.readPools : tableRule.writePools);
                        if (pools == null || pools.length == 0) {
                            pools = tableRule.defaultPools;
                        }
                        
                        for (String poolName : pools) {
                            if (!poolNames.contains(poolName)) {
                                poolNames.add(poolName);
                            }
                        }
                        
                        if(!isPrepared){
                           if (logger.isDebugEnabled()) {
                           	loggerBuffer.append(", no Column rule, using table:" + tableRule.table + " default rules:" + Arrays.toString(tableRule.defaultPools));
                           }
                        }
                        continue;
                    }

                    List<String> groupMatched = new ArrayList<String>();
                    for (Rule rule : tableRule.ruleList) {
                        if (rule.group != null) {
                            if (groupMatched.contains(rule.group)) {
                                continue;
                            }
                        }

                        // 如果参数比必须的参数个数少，则继续下一条规则
                        if (columnMap.size() < rule.parameterMap.size()) {
                            continue;
                        } else {
                            boolean matched = true;
                            // 如果查询语句中包含了该规则不需要的参数，则该规则将被忽略
                            for (Column exclude : rule.excludes) {
                                Comparable<?> condition = columnMap.get(exclude);
                                if (condition != null) {
                                    matched = false;
                                    break;
                                }
                            }

                            // 如果不匹配将继续下一条规则
                            if (!matched) {
                                continue;
                            }

                            Comparable<?>[] comparables = new Comparable[rule.parameterMap.size()];
                            // 规则中的参数必须在dmlstatement中存在，否则这个规则将不启作用
                            for (Map.Entry<Column, Integer> parameter : rule.cloumnMap.entrySet()) {
                            	Comparative condition = null;
                            	if(regexMatched){
	                            	Column column = new Column();
	                            	column.setName(parameter.getKey().getName());
	                            	column.setTable(table);
	                                condition = columnMap.get(column);
                            	}else{
                            		condition = columnMap.get(parameter.getKey());
                            	}
                            	
                                if (condition != null) {
                                    // 如果规则忽略 数组的 参数，并且参数有array 参数，则忽略该规则
                                    if (rule.ignoreArray && condition instanceof ComparativeBaseList) {
                                        matched = false;
                                        break;
                                    }

                                    comparables[parameter.getValue()] = (Comparative) condition.clone();
                                } else {
                                    matched = false;
                                    break;
                                }
                            }

                            // 如果不匹配将继续下一条规则
                            if (!matched) {
                                continue;
                            }
                            
                            try {
                                Comparable<?> result = rule.rowJep.getValue(comparables);
                                Integer i = 0;
                                if (result instanceof Comparative) {
                                    if (rule.result == RuleResult.INDEX) {
                                        i = (Integer) ((Comparative) result).getValue();
                                        if (i < 0) {
                                            continue;
                                        }
                                        matched = true;
                                    } else if(rule.result == RuleResult.POOLNAME){
                                    	String matchedPoolsString = ((Comparative) result).getValue().toString();
                                    	String[] poolNamesMatched = matchedPoolsString.split(",");
                                    	
                                    	if(poolNamesMatched != null && poolNamesMatched.length >0){
                                       	for(String poolName : poolNamesMatched){
                                           	if (!poolNames.contains(poolName)) {
                                                   poolNames.add(poolName);
                                               }
                                       	}
                                       	
                                       	if (logger.isDebugEnabled()) {
                                       		loggerBuffer.append(", matched table:" + tableRule.table + ", rule:" + rule.name);
                                           }
                                    	}
                                    	continue;
                                    }else{
                                        matched = (Boolean) ((Comparative) result).getValue();
                                    }
                                } else {
                                	
                                	if (rule.result == RuleResult.INDEX) {
                                        i = (Integer) Integer.valueOf(result.toString());
                                        if (i < 0) {
                                            continue;
                                        }
                                        matched = true;
                                    } else if(rule.result == RuleResult.POOLNAME){
                                    	String matchedPoolsString = result.toString();
                                    	String[] poolNamesMatched = StringUtil.split(matchedPoolsString,";,");
                                    	if(poolNamesMatched != null && poolNamesMatched.length >0){
                                       	for(String poolName : poolNamesMatched){
                                           	if (!poolNames.contains(poolName)) {
                                                   poolNames.add(poolName);
                                               }
                                       	}
                                       	
                                       	if (logger.isDebugEnabled()) {
                                       		loggerBuffer.append(", matched table:" + tableRule.table + ", rule:" + rule.name);
                                           }
                                    	}
                                    	continue;
                                    }else{
                                    	matched = (Boolean) result;
                                    }
                                }

                                if (matched) {
                                    if (rule.group != null) {
                                        groupMatched.add(rule.group);
                                    }
                                    String[] pools = (isRead ? rule.readPools : rule.writePools);
                                    if (pools == null || pools.length == 0) {
                                        pools = rule.defaultPools;
                                    }
                                    if (pools != null && pools.length > 0) {
                                        if (rule.isSwitch) {
                                            if (!poolNames.contains(pools[i])) {
                                                poolNames.add(pools[i]);
                                            }
                                        } else {
                                            for (String poolName : pools) {
                                                if (!poolNames.contains(poolName)) {
                                                    poolNames.add(poolName);
                                                }
                                            }
                                        }
                                    } else {
                                        logger.error("rule:" + rule.name + " matched, but pools is null");
                                    }

                                    if (logger.isDebugEnabled()) {
                                   	 loggerBuffer.append(", matched table:" + tableRule.table + ", rule:" + rule.name);
                                    }
                                }
                            } catch (com.meidusa.amoeba.sqljep.ParseException e) {
                                // logger.error("parse rule error:"+rule.expression,e);
                            }
                        }
                    }

                    // 如果所有规则都无法匹配，则默认采用TableRule中的pool设置。
                    if (poolNames.size() == 0) {
                        String[] pools = (isRead ? tableRule.readPools : tableRule.writePools);
                        if (pools == null || pools.length == 0) {
                            pools = tableRule.defaultPools;
                        }
                        
                        if(!isPrepared){
                        	if(tableRule.ruleList != null && tableRule.ruleList.size()>0){
                        		if (logger.isDebugEnabled()) {
                        			loggerBuffer.append(", no rule matched, using tableRule:[" + tableRule.table + "] defaultPools");
                        		}
                        	}else{
                        		if(logger.isDebugEnabled()){
                        			if(pools != null){
                        				StringBuffer buffer = new StringBuffer();
                           			for(String pool : pools){
                           				buffer.append(pool).append(",");
                           			}
                           			loggerBuffer.append(", using tableRule:[" + tableRule.table + "] defaultPools="+buffer.toString());
                        			}
                        		}
                        	}
                        }
                        for (String poolName : pools) {
                            if (!poolNames.contains(poolName)) {
                                poolNames.add(poolName);
                            }
                        }
                    }
                }
            }
        }
        return poolNames;
    }
    
    public ObjectPool[] selectPool(T connection, V queryObject){
    	beforeSelectPool(connection,queryObject);
    	
    	StringBuffer loggerBuffer = null;
    	
    	boolean isRead = true;
		if (queryObject instanceof Request) {
 			isRead = ((Request) queryObject).isRead();
 		}
		
		if (logger.isDebugEnabled()) {
			loggerBuffer = new StringBuffer("query=");
			loggerBuffer.append(queryObject);
			if(queryObject instanceof Request){
				loggerBuffer.append(((Request)queryObject).isPrepared()?",prepared=true":"");
			}
		}
		List<String> poolNames = new ArrayList<String>();
    	 poolNames = evaluate(loggerBuffer,connection,queryObject);
         ObjectPool[] pools = new ObjectPool[poolNames.size()];
         int i = 0;
         for (String name : poolNames) {
         	ObjectPool pool = ProxyRuntimeContext.getInstance().getPoolMap().get(name);
         	if(pool == null){
         		logger.error("cannot found Pool="+name+",sqlObject="+queryObject);
         		throw new RuntimeException("cannot found Pool="+name+",sqlObject="+queryObject);
         	}
         	pools[i++] = pool;
         }

         if (pools == null || pools.length == 0) {
             pools = (isRead ? this.readPools : this.writePools);
             if (logger.isDebugEnabled() && pools != null && pools.length > 0) {
                 if (isRead) {
                	 loggerBuffer.append(",  route to queryRouter readPool:" + readPool + "\r\n");
                 } else {
                	 loggerBuffer.append(",  route to queryRouter writePool:" + writePool + "\r\n");
                 }
             }

             if (pools == null || pools.length == 0) {
                 pools = this.defaultPools;
                 if (logger.isDebugEnabled() && pools != null && pools.length > 0) {
                	 loggerBuffer.append(",  route to queryRouter defaultPool:" + defaultPool + "\r\n");
                 }
             }
         } else {
             if (logger.isDebugEnabled() && pools != null && pools.length > 0) {
            	 loggerBuffer.append(",  route to pools:" + poolNames + "\r\n");
             }
         }
         
         
    	 if(logger.isDebugEnabled()){
    		 if(loggerBuffer != null){
    			 logger.debug(loggerBuffer.toString());
    		 }
         }
         return pools;
    }
  
    public void doChange(){
    	defaultPools = new ObjectPool[] { ProxyRuntimeContext.getInstance().getPoolMap().get(defaultPool) };

    	if (readPool != null && !StringUtil.isEmpty(readPool)) {
            readPools = new ObjectPool[] { ProxyRuntimeContext.getInstance().getPoolMap().get(readPool) };
        }
        if (writePool != null && !StringUtil.isEmpty(writePool)) {
            writePools = new ObjectPool[] { ProxyRuntimeContext.getInstance().getPoolMap().get(writePool) };
        }
    }
    
    public void init() throws InitialisationException {
        defaultPools = new ObjectPool[] { ProxyRuntimeContext.getInstance().getPoolMap().get(defaultPool) };

        if (defaultPools == null || defaultPools[0] == null) {
            throw new InitialisationException("default pool required!,defaultPool="+defaultPool +" invalid");
        }
        if (readPool != null && !StringUtil.isEmpty(readPool)) {
        	ObjectPool pool = ProxyRuntimeContext.getInstance().getPoolMap().get(readPool);
        	if(pool == null){
         		logger.error("cannot found Pool="+readPool);
         		throw new InitialisationException("cannot found Pool="+readPool);
         	}
            readPools = new ObjectPool[] { pool };
        }
        if (writePool != null && !StringUtil.isEmpty(writePool)) {
        	ObjectPool pool = ProxyRuntimeContext.getInstance().getPoolMap().get(writePool);
        	if(pool == null){
         		logger.error("cannot found Pool="+writePool);
         		throw new InitialisationException("cannot found Pool="+writePool);
         	}
            writePools = new ObjectPool[] { pool };
        }
        
        map = new LRUMap(LRUMapSize);

        class ConfigCheckTread extends Thread {

            long lastFunFileModified;

            private ConfigCheckTread(){

                this.setDaemon(true);
                this.setName("ruleConfigCheckThread");
                if(sqlFunctionFile != null){
                	lastFunFileModified = sqlFunctionFile.lastModified();
                }
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000l);
                        Map<String, Function> funMap = null;
                        Map<Table, TableRule> tableRuleMap = null;
                        try {
                            if (AbstractQueryRouter.this.sqlFunctionFile != null) {
                                if (sqlFunctionFile.lastModified() != lastFunFileModified) {
                                    try {
                                        funMap = loadFunctionMap(AbstractQueryRouter.this.sqlFunctionFile.getAbsolutePath());
                                        logger.info("loading FunctionMap from File="+sqlFunctionFile);
                                    } catch (ConfigurationException exception) {
                                    }

                                }
                            }

                            if(ruleLoader.needLoad()){
                                tableRuleMap = ruleLoader.loadRule();
                                if(tableRuleMap != null){
                                	for(Map.Entry<Table, TableRule> ruleEntry:tableRuleMap.entrySet()){
                                		Table ruleTable = ruleEntry.getKey();
                                		if(ruleTable.getName().indexOf("*")>=0 
                                				|| (ruleTable.getSchema().getName() != null && ruleTable.getSchema().getName().indexOf("*")>=0)
                                				|| ruleTable.getName().indexOf("^")>=0 
                                				|| (ruleTable.getSchema().getName() != null && ruleTable.getSchema().getName().indexOf("^")>=0)){
                                			getPattern(ruleTable.getName());
                                			regexTableRuleMap.put(ruleTable, ruleEntry.getValue());
                                		}
                                	}
                                }
                            }

                            if (funMap != null) {
                                AbstractQueryRouter.this.functionMap = funMap;
                            }

                            if (tableRuleMap != null) {
                                AbstractQueryRouter.this.tableRuleMap = tableRuleMap;
                            }
                        } catch (ConfigurationException e) {
                        } finally {
                            if (sqlFunctionFile != null && sqlFunctionFile.exists()) {
                                lastFunFileModified = sqlFunctionFile.lastModified();
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        if (needParse) {
        	
            if (AbstractQueryRouter.this.sqlFunctionFile != null) {
                this.functionMap = loadFunctionMap(AbstractQueryRouter.this.sqlFunctionFile.getAbsolutePath());
            }
            
            this.tableRuleMap = ruleLoader.loadRule();
            if(tableRuleMap != null){
            	for(Map.Entry<Table, TableRule> ruleEntry:this.tableRuleMap.entrySet()){
            		Table ruleTable = ruleEntry.getKey();
            		if(ruleTable.getName().indexOf("*")>=0 
            				|| (ruleTable.getSchema().getName() != null && ruleTable.getSchema().getName().indexOf("*")>=0)
            				|| ruleTable.getName().indexOf("^")>=0 
            				|| (ruleTable.getSchema().getName() != null && ruleTable.getSchema().getName().indexOf("^")>=0)){
            			this.getPattern(ruleTable.getName());
            			regexTableRuleMap.put(ruleTable, ruleEntry.getValue());
            		}
            	}
            }
            
            new ConfigCheckTread().start();
        }
    }

    public static Map<String, Function> loadFunctionMap(String configFileName) {
        FunctionLoader<String, Function> loader = new FunctionLoader<String, Function>() {

            @Override
            public void initBeanObject(BeanObjectEntityConfig config, Function bean) {
                bean.setName(config.getName());
            }

            @Override
            public void putToMap(Map<String, Function> map, Function value) {
                map.put(value.getName(), value);
            }

        };

        loader.setDTD("/com/meidusa/amoeba/xml/function.dtd");
        loader.setDTDSystemID("function.dtd");
        logger.info("loading FunctionMap from File="+configFileName);
        return loader.loadFunctionMap(configFileName);
    }

    public int getLRUMapSize() {
        return LRUMapSize;
    }

    public String getDefaultPool() {
        return defaultPool;
    }

    public void setDefaultPool(String defaultPoolName) {
        this.defaultPool = defaultPoolName;
    }

    public void setLRUMapSize(int mapSize) {
        LRUMapSize = mapSize;
    }

    public File getSqlFunctionFile() {
        return sqlFunctionFile;
    }

    public void setSqlFunctionFile(File sqlFunctionFile) {
        this.sqlFunctionFile = sqlFunctionFile;
    }

    public boolean isNeedParse() {
        return needParse;
    }

    public void setNeedParse(boolean needParse) {
        this.needParse = needParse;
    }

    public ObjectPool getObjectPool(Object key) {
        if (key instanceof String) {
            return ProxyRuntimeContext.getInstance().getPoolMap().get(key);
        } else {
            for (ObjectPool pool : ProxyRuntimeContext.getInstance().getPoolMap().values()) {
                if (pool.hashCode() == key.hashCode()) {
                    return pool;
                }
            }
        }
        return null;
    }

    public ObjectPool[] getDefaultObjectPool(){
    	return this.defaultPools;
    }
    
    private Pattern getPattern(String source){
    	if(source != null && source.indexOf("*") ==0){
    		source = "^"+source;
    	}
    	Pattern pattern = this.patternMap.get(source);
    	if(pattern == null){
    		synchronized (patternMap) {
    			pattern = this.patternMap.get(source);
    			if(pattern == null){
    				pattern = Pattern.compile(source);
    			}
    			patternMap.put(source, pattern);
			}
    	}
    	return pattern;
    }
    public static void main(String[] aa){
    	String[] aaa = StringUtil.split("asdfasdf,asdf;aqwer",";,");
    	for(String aaaaa : aaa){
    		System.out.println(aaaaa);
    	}
    	System.out.println(System.currentTimeMillis());
    	String source = "^fileSys_[a-zA-Z0-9_]*";
    	Pattern pattern = null;
    	if(pattern == null){
			pattern = Pattern.compile(source);
		}
    	java.util.regex.Matcher matcher = pattern.matcher("fileSys_abc12d");
		System.out.println(matcher.matches());
    }
    
    
}
