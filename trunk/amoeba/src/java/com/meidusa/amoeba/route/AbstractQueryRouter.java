/**
 * <pre>
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.route;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.map.LRUMap;
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
import com.meidusa.amoeba.config.DocumentUtil;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.ParseException;
import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.dbobject.Schema;
import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.parser.function.Function;
import com.meidusa.amoeba.parser.statment.CommitStatment;
import com.meidusa.amoeba.parser.statment.DMLStatment;
import com.meidusa.amoeba.parser.statment.PropertyStatment;
import com.meidusa.amoeba.parser.statment.RollbackStatment;
import com.meidusa.amoeba.parser.statment.ShowStatment;
import com.meidusa.amoeba.parser.statment.StartTansactionStatment;
import com.meidusa.amoeba.parser.statment.Statment;
import com.meidusa.amoeba.sqljep.RowJEP;
import com.meidusa.amoeba.sqljep.function.Abs;
import com.meidusa.amoeba.sqljep.function.AddDate;
import com.meidusa.amoeba.sqljep.function.AddMonths;
import com.meidusa.amoeba.sqljep.function.AddTime;
import com.meidusa.amoeba.sqljep.function.Case;
import com.meidusa.amoeba.sqljep.function.Ceil;
import com.meidusa.amoeba.sqljep.function.Comparative;
import com.meidusa.amoeba.sqljep.function.ComparativeBaseList;
import com.meidusa.amoeba.sqljep.function.Concat;
import com.meidusa.amoeba.sqljep.function.Datediff;
import com.meidusa.amoeba.sqljep.function.Day;
import com.meidusa.amoeba.sqljep.function.DayName;
import com.meidusa.amoeba.sqljep.function.DayOfWeek;
import com.meidusa.amoeba.sqljep.function.DayOfYear;
import com.meidusa.amoeba.sqljep.function.Decode;
import com.meidusa.amoeba.sqljep.function.Floor;
import com.meidusa.amoeba.sqljep.function.Hash;
import com.meidusa.amoeba.sqljep.function.Hour;
import com.meidusa.amoeba.sqljep.function.IndistinctMatching;
import com.meidusa.amoeba.sqljep.function.Initcap;
import com.meidusa.amoeba.sqljep.function.Instr;
import com.meidusa.amoeba.sqljep.function.LastDay;
import com.meidusa.amoeba.sqljep.function.Length;
import com.meidusa.amoeba.sqljep.function.Lower;
import com.meidusa.amoeba.sqljep.function.Lpad;
import com.meidusa.amoeba.sqljep.function.Ltrim;
import com.meidusa.amoeba.sqljep.function.MakeDate;
import com.meidusa.amoeba.sqljep.function.MakeTime;
import com.meidusa.amoeba.sqljep.function.Microsecond;
import com.meidusa.amoeba.sqljep.function.Minute;
import com.meidusa.amoeba.sqljep.function.Modulus;
import com.meidusa.amoeba.sqljep.function.Month;
import com.meidusa.amoeba.sqljep.function.MonthName;
import com.meidusa.amoeba.sqljep.function.MonthsBetween;
import com.meidusa.amoeba.sqljep.function.NextDay;
import com.meidusa.amoeba.sqljep.function.Nvl;
import com.meidusa.amoeba.sqljep.function.PostfixCommand;
import com.meidusa.amoeba.sqljep.function.Power;
import com.meidusa.amoeba.sqljep.function.Replace;
import com.meidusa.amoeba.sqljep.function.Round;
import com.meidusa.amoeba.sqljep.function.Rpad;
import com.meidusa.amoeba.sqljep.function.Rtrim;
import com.meidusa.amoeba.sqljep.function.Second;
import com.meidusa.amoeba.sqljep.function.Sign;
import com.meidusa.amoeba.sqljep.function.SubDate;
import com.meidusa.amoeba.sqljep.function.SubTime;
import com.meidusa.amoeba.sqljep.function.Substring;
import com.meidusa.amoeba.sqljep.function.ToChar;
import com.meidusa.amoeba.sqljep.function.ToDate;
import com.meidusa.amoeba.sqljep.function.ToNumber;
import com.meidusa.amoeba.sqljep.function.Translate;
import com.meidusa.amoeba.sqljep.function.Trim;
import com.meidusa.amoeba.sqljep.function.Trunc;
import com.meidusa.amoeba.sqljep.function.Upper;
import com.meidusa.amoeba.sqljep.function.WeekOfYear;
import com.meidusa.amoeba.sqljep.function.Year;
import com.meidusa.amoeba.sqljep.variable.Variable;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.StringUtil;

/**
 * @author struct
 * @author hexianmao
 */
@SuppressWarnings("deprecation")
public abstract class AbstractQueryRouter implements QueryRouter, Initialisable {

    private static Logger                           logger          = Logger.getLogger(AbstractQueryRouter.class);

    public final static Map<String, PostfixCommand> ruleFunTab      = new HashMap<String, PostfixCommand>();
    static {
        ruleFunTab.put("abs", new Abs());
        ruleFunTab.put("power", new Power());
        ruleFunTab.put("mod", new Modulus());
        ruleFunTab.put("substr", new Substring());
        ruleFunTab.put("sign", new Sign());
        ruleFunTab.put("ceil", new Ceil());
        ruleFunTab.put("floor", new Floor());
        ruleFunTab.put("trunc", new Trunc());
        ruleFunTab.put("round", new Round());
        ruleFunTab.put("length", new Length());
        ruleFunTab.put("concat", new Concat());
        ruleFunTab.put("instr", new Instr());
        ruleFunTab.put("trim", new Trim());
        ruleFunTab.put("rtrim", new Rtrim());
        ruleFunTab.put("ltrim", new Ltrim());
        ruleFunTab.put("rpad", new Rpad());
        ruleFunTab.put("lpad", new Lpad());
        ruleFunTab.put("lower", new Lower());
        ruleFunTab.put("upper", new Upper());
        ruleFunTab.put("translate", new Translate());
        ruleFunTab.put("replace", new Replace());
        ruleFunTab.put("initcap", new Initcap());
        ruleFunTab.put("value", new Nvl());
        ruleFunTab.put("decode", new Decode());
        ruleFunTab.put("to_char", new ToChar());
        ruleFunTab.put("to_number", new ToNumber());
        ruleFunTab.put("imatch", new IndistinctMatching()); // replacement for of Oracle's SOUNDEX
        ruleFunTab.put("months_between", new MonthsBetween());
        ruleFunTab.put("add_months", new AddMonths());
        ruleFunTab.put("last_day", new LastDay());
        ruleFunTab.put("next_day", new NextDay());
        ruleFunTab.put("to_date", new ToDate());
        ruleFunTab.put("case", new Case()); // replacement for CASE WHEN digit = 0 THEN ...;WHEN digit = 1
        // THEN...;ELSE... END CASE
        ruleFunTab.put("index", new Instr()); // maxdb
        ruleFunTab.put("num", new ToNumber()); // maxdb
        ruleFunTab.put("chr", new ToChar()); // maxdb
        ruleFunTab.put("dayname", new DayName()); // maxdb
        ruleFunTab.put("adddate", new AddDate()); // maxdb
        ruleFunTab.put("subdate", new SubDate()); // maxdb
        ruleFunTab.put("addtime", new AddTime()); // maxdb
        ruleFunTab.put("subtime", new SubTime()); // maxdb
        ruleFunTab.put("year", new Year()); // maxdb
        ruleFunTab.put("month", new Month()); // maxdb
        ruleFunTab.put("day", new Day()); // maxdb
        ruleFunTab.put("dayofmonth", new Day()); // maxdb
        ruleFunTab.put("hour", new Hour()); // maxdb
        ruleFunTab.put("minute", new Minute()); // maxdb
        ruleFunTab.put("second", new Second()); // maxdb
        ruleFunTab.put("microsecond", new Microsecond()); // maxdb
        ruleFunTab.put("datediff", new Datediff()); // maxdb
        ruleFunTab.put("dayofweek", new DayOfWeek()); // maxdb
        ruleFunTab.put("weekofyear", new WeekOfYear()); // maxdb
        ruleFunTab.put("dayofyear", new DayOfYear()); // maxdb
        ruleFunTab.put("dayname", new DayName()); // maxdb
        ruleFunTab.put("monthname", new MonthName()); // maxdb
        ruleFunTab.put("makedate", new MakeDate()); // maxdb
        ruleFunTab.put("maketime", new MakeTime()); // maxdb
        ruleFunTab.put("hash", new Hash()); //
    }

    /* 默认1000 */
    private int                                     LRUMapSize      = 1000;
    private LRUMap                                  map;
    private Lock                                    mapLock         = new ReentrantLock(false);

    private Map<Table, TableRule>                   tableRuleMap    = new HashMap<Table, TableRule>();
    private Map<String, Function>                   functionMap     = new HashMap<String, Function>();
    private Map<String, PostfixCommand>             ruleFunctionMap = new HashMap<String, PostfixCommand>();

    protected ObjectPool[]                          defaultPools;
    protected ObjectPool[]                          readPools;
    protected ObjectPool[]                          writePools;

    private String                                  ruleConfig;
    private String                                  functionConfig;
    private String                                  ruleFunctionConfig;

    private String                                  defaultPool;
    private String                                  readPool;
    private String                                  writePool;

    private boolean                                 needParse       = true;
    private boolean                                 needEvaluate    = true;

    public AbstractQueryRouter(){
        ruleFunctionMap.putAll(ruleFunTab);
    }

    public String getRuleConfig() {
        return ruleConfig;
    }

    public abstract Parser newParser(String sql);

    public void setRuleConfig(String ruleConfig) {
        this.ruleConfig = ruleConfig;
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

    public ObjectPool[] doRoute(DatabaseConnection connection, String sql, boolean ispreparedStatment,
                                Object[] parameters) {
        if (sql == null) {
            return defaultPools;
        }
        if (needParse) {
            return selectPool(connection, sql, ispreparedStatment, parameters);
        } else {
            return defaultPools;
        }
    }

    /**
     * 返回Query 被route到目标地址 ObjectPool集合 如果返回null，则是属于DatabaseConnection 自身属性设置的请求。
     */
    protected ObjectPool[] selectPool(DatabaseConnection connection, String sql, boolean ispreparedStatment,
                                      Object[] parameters) {
        List<String> poolNames = new ArrayList<String>();

        Statment statment = parseSql(connection, sql);
        DMLStatment dmlStatment = null;

        if (statment instanceof DMLStatment) {
            if (logger.isDebugEnabled()) {
                logger.debug("DMLStatement:[" + sql + "] Expression=[" + statment.getExpression() + "]");
            }

            dmlStatment = (DMLStatment) statment;
            Map<Table, Map<Column, Comparative>> tables = null;
            if (needEvaluate) {
                tables = dmlStatment.evaluate(parameters);
                if (tables != null && tables.size() > 0) {
                    Set<Map.Entry<Table, Map<Column, Comparative>>> entrySet = tables.entrySet();
                    for (Map.Entry<Table, Map<Column, Comparative>> entry : entrySet) {
                        Map<Column, Comparative> columnMap = entry.getValue();
                        TableRule tableRule = this.tableRuleMap.get(entry.getKey());

                        // 如果存在table Rule 则需要看是否有Rule
                        if (tableRule != null) {
                            // 没有列的sql语句，使用默认的tableRule
                            if (columnMap == null || ispreparedStatment) {
                                String[] pools = dmlStatment.isReadStatment() ? tableRule.readPools : tableRule.writePools;
                                if (pools == null || pools.length == 0) {
                                    pools = tableRule.defaultPools;
                                }
                                for (String poolName : pools) {
                                    if (!poolNames.contains(poolName)) {
                                        poolNames.add(poolName);
                                    }
                                }
                                if (logger.isDebugEnabled()) {
                                    logger.debug("[" + sql + "] no Column rule, using table:" + tableRule.table + " default rules:" + Arrays.toString(tableRule.defaultPools));
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

                                // 如果参数比必须的参数小，则继续下一条规则
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
                                        Comparative condition = columnMap.get(parameter.getKey());
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
                                            if (rule.isSwitch) {
                                                i = (Integer) ((Comparative) result).getValue();
                                                if (i < 0) {
                                                    continue;
                                                }
                                                matched = true;
                                            } else {
                                                matched = (Boolean) ((Comparative) result).getValue();
                                            }
                                        } else {
                                            matched = (Boolean) result;
                                        }

                                        if (matched) {
                                            if (rule.group != null) {
                                                groupMatched.add(rule.group);
                                            }
                                            String[] pools = dmlStatment.isReadStatment() ? rule.readPools : rule.writePools;
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
                                                logger.debug("[" + sql + "] matched table:" + tableRule.table.getName() + ", rule:" + rule.name);
                                            }
                                        }
                                    } catch (com.meidusa.amoeba.sqljep.ParseException e) {
                                        // logger.error("parse rule error:"+rule.expression,e);
                                    }
                                }
                            }

                            // 如果所有规则都无法匹配，则默认采用TableRule中的pool设置。
                            if (poolNames.size() == 0) {
                                String[] pools = dmlStatment.isReadStatment() ? tableRule.readPools : tableRule.writePools;
                                if (pools == null || pools.length == 0) {
                                    pools = tableRule.defaultPools;
                                }
                                logger.warn("no rule matched, using tableRule:[" + tableRule.table.getName() + "] defaultPools");

                                for (String poolName : pools) {
                                    if (!poolNames.contains(poolName)) {
                                        poolNames.add(poolName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (statment instanceof PropertyStatment) {
            if (logger.isDebugEnabled()) {
                logger.debug("PropertyStatment:[" + sql + "]");
            }
            setProperty(connection, (PropertyStatment) statment, parameters);
            return null;
        } else if (statment instanceof ShowStatment) {
            if (logger.isDebugEnabled()) {
                logger.debug("ShowStatment:[" + sql + "]");
            }
            TableRule tableRule = this.tableRuleMap.get(null);
            if (tableRule != null && tableRule.defaultPools != null && tableRule.defaultPools.length > 0) {
                for (String poolName : tableRule.defaultPools) {
                    if (!poolNames.contains(poolName)) {
                        poolNames.add(poolName);
                    }
                }
            }
        } else if (statment instanceof StartTansactionStatment) {
            if (logger.isDebugEnabled()) {
                logger.debug("StartTansactionStatment:[" + sql + "]");
            }
            return null;
        } else if (statment instanceof CommitStatment) {
            if (logger.isDebugEnabled()) {
                logger.debug("CommitStatment:[" + sql + "]");
            }
            return null;
        } else if (statment instanceof RollbackStatment) {
            if (logger.isDebugEnabled()) {
                logger.debug("RollbackStatment:[" + sql + "]");
            }
            return null;
        } else {
            throw new RuntimeException("error,unknown statement:[" + sql + "]");
        }

        ObjectPool[] pools = new ObjectPool[poolNames.size()];
        int i = 0;
        for (String name : poolNames) {
            pools[i++] = ProxyRuntimeContext.getInstance().getPoolMap().get(name);
        }

        if (pools == null || pools.length == 0) {
            if (dmlStatment != null) {
                pools = dmlStatment.isReadStatment() ? this.readPools : this.writePools;
                if (logger.isDebugEnabled() && pools != null && pools.length > 0) {
                    if (dmlStatment.isReadStatment()) {
                        logger.debug("[" + sql + "] route to queryRouter readPool:" + readPool + "\n");
                    } else {
                        logger.debug("[" + sql + "] route to queryRouter writePool:" + writePool + "\n");
                    }
                }
            }

            if (pools == null || pools.length == 0) {
                pools = this.defaultPools;
                if (logger.isDebugEnabled() && pools != null && pools.length > 0) {
                    logger.debug("[" + sql + "] route to queryRouter defaultPool:" + defaultPool + "\n");
                }
            }
        } else {
            if (logger.isDebugEnabled() && pools != null && pools.length > 0) {
                logger.debug("[" + sql + "] route to pools:" + poolNames + "\n");
            }
        }

        return pools;
    }

    /**
     * 根据 PropertyStatment 设置相关连接的属性
     * 
     * @param conn 当前请求的连接
     * @param statment 当前请求的Statment
     * @param parameters
     */
    protected abstract void setProperty(DatabaseConnection conn, PropertyStatment statment, Object[] parameters);

    public void init() throws InitialisationException {
        defaultPools = new ObjectPool[] { ProxyRuntimeContext.getInstance().getPoolMap().get(defaultPool) };

        if (defaultPools == null || defaultPools[0] == null) {
            throw new InitialisationException("default pool required!");
        }
        if (readPool != null && !StringUtil.isEmpty(readPool)) {
            readPools = new ObjectPool[] { ProxyRuntimeContext.getInstance().getPoolMap().get(readPool) };
        }
        if (writePool != null && !StringUtil.isEmpty(writePool)) {
            writePools = new ObjectPool[] { ProxyRuntimeContext.getInstance().getPoolMap().get(writePool) };
        }
        map = new LRUMap(LRUMapSize);

        class ConfigCheckTread extends Thread {

            long lastRuleModified;
            long lastFunFileModified;
            long lastRuleFunctionFileModified;
            File ruleFile;
            File funFile;
            File ruleFunctionFile;

            private ConfigCheckTread(){

                this.setDaemon(true);
                this.setName("ruleConfigCheckThread");
                ruleFile = new File(AbstractQueryRouter.this.ruleConfig);
                funFile = new File(AbstractQueryRouter.this.functionConfig);
                lastRuleModified = ruleFile.lastModified();
                lastFunFileModified = funFile.lastModified();
                if (AbstractQueryRouter.this.ruleFunctionConfig != null) {
                    ruleFunctionFile = new File(AbstractQueryRouter.this.ruleFunctionConfig);
                    lastRuleFunctionFileModified = ruleFunctionFile.lastModified();
                }
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000l);
                        Map<String, Function> funMap = null;
                        Map<String, PostfixCommand> ruleFunMap = null;
                        Map<Table, TableRule> tableRuleMap = null;
                        try {
                            if (AbstractQueryRouter.this.functionConfig != null) {
                                if (funFile.lastModified() != lastFunFileModified) {
                                    try {
                                        funMap = loadFunctionMap(AbstractQueryRouter.this.functionConfig);
                                    } catch (ConfigurationException exception) {
                                    }

                                }
                            }
                            if (AbstractQueryRouter.this.ruleFunctionConfig != null) {
                                if (ruleFunctionFile.lastModified() != lastRuleFunctionFileModified) {
                                    ruleFunMap = loadRuleFunctionMap(AbstractQueryRouter.this.ruleFunctionConfig);
                                }
                            }

                            if (AbstractQueryRouter.this.ruleConfig != null) {
                                if (ruleFile.lastModified() != lastRuleModified || (AbstractQueryRouter.this.ruleFunctionConfig != null && ruleFunctionFile.lastModified() != lastRuleFunctionFileModified)) {
                                    tableRuleMap = loadConfig(AbstractQueryRouter.this.ruleConfig);
                                }
                            }

                            if (funMap != null) {
                                AbstractQueryRouter.this.functionMap = funMap;
                            }

                            if (ruleFunMap != null) {
                                AbstractQueryRouter.this.ruleFunctionMap = ruleFunMap;
                            }

                            if (tableRuleMap != null) {
                                AbstractQueryRouter.this.tableRuleMap = tableRuleMap;
                            }
                        } catch (ConfigurationException e) {
                        } finally {
                            if (funFile != null && funFile.exists()) {
                                lastFunFileModified = funFile.lastModified();
                            }
                            if (ruleFunctionFile != null && ruleFunctionFile.exists()) {
                                lastRuleFunctionFileModified = ruleFunctionFile.lastModified();
                            }
                            if (ruleFile != null && ruleFile.exists()) {
                                lastRuleModified = ruleFile.lastModified();
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        if (needParse) {
            boolean configNeedCheck = false;

            if (AbstractQueryRouter.this.functionConfig != null) {
                this.functionMap = loadFunctionMap(AbstractQueryRouter.this.functionConfig);
                configNeedCheck = true;
            } else {
                needEvaluate = false;
            }

            if (AbstractQueryRouter.this.ruleFunctionConfig != null) {
                AbstractQueryRouter.this.ruleFunctionMap = loadRuleFunctionMap(AbstractQueryRouter.this.ruleFunctionConfig);
                configNeedCheck = true;
            }

            if (AbstractQueryRouter.this.ruleConfig != null) {
                this.tableRuleMap = loadConfig(this.ruleConfig);
                configNeedCheck = true;
            } else {
                needEvaluate = false;
            }

            if (configNeedCheck) {
                new ConfigCheckTread().start();
            }
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
        return loader.loadFunctionMap(configFileName);
    }

    public static Map<String, PostfixCommand> loadRuleFunctionMap(String configFileName) {
        FunctionLoader<String, PostfixCommand> loader = new FunctionLoader<String, PostfixCommand>() {

            @Override
            public void initBeanObject(BeanObjectEntityConfig config, PostfixCommand bean) {
                bean.setName(config.getName());
            }

            @Override
            public void putToMap(Map<String, PostfixCommand> map, PostfixCommand value) {
                map.put(value.getName(), value);
            }

        };

        loader.setDTD("/com/meidusa/amoeba/xml/function.dtd");
        loader.setDTDSystemID("function.dtd");

        Map<String, PostfixCommand> tempRuleFunMap = new HashMap<String, PostfixCommand>();
        Map<String, PostfixCommand> defindMap = loader.loadFunctionMap(configFileName);
        tempRuleFunMap.putAll(ruleFunTab);
        tempRuleFunMap.putAll(defindMap);
        return tempRuleFunMap;
    }

    private Map<Table, TableRule> loadConfig(String configFileName) {
        DocumentBuilder db;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            dbf.setNamespaceAware(false);
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(new EntityResolver() {

                public InputSource resolveEntity(String publicId, String systemId) {
                    if (systemId.endsWith("rule.dtd")) {
                        InputStream in = AbstractQueryRouter.class.getResourceAsStream("/com/meidusa/amoeba/xml/rule.dtd");
                        if (in == null) {
                            LogLog.error("Could not find [rule.dtd]. Used [" + AbstractQueryRouter.class.getClassLoader() + "] class loader in the search.");
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

    private Map<Table, TableRule> loadConfigurationFile(String fileName, DocumentBuilder db)
                                                                                            throws InitialisationException {
        Document doc = null;
        InputStream is = null;
        Map<Table, TableRule> tableRuleMap = new HashMap<Table, TableRule>();
        try {
            is = new FileInputStream(new File(fileName));
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
                if (nodeName.equals("tableRule")) {
                    TableRule rule = loadTableRule(child);
                    tableRuleMap.put(rule.table.getName() == null ? null : rule.table, rule);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Loaded rule configuration from: " + fileName);
        }
        return tableRuleMap;
    }

    private TableRule loadTableRule(Element current) throws InitialisationException {
        TableRule tableRule = new TableRule();
        String name = current.getAttribute("name");
        String schemaName = current.getAttribute("schema");
        Table table = new Table();
        table.setName(name);
        if (!StringUtil.isEmpty(schemaName)) {
            Schema schema = new Schema();

            schema.setName(schemaName);
            table.setSchema(schema);
        }

        tableRule.table = table;
        String defaultPools = current.getAttribute("defaultPools");
        if (defaultPools != null) {
            tableRule.defaultPools = readTokenizedString(defaultPools, " ,");
        }

        String readPools = current.getAttribute("readPools");
        if (readPools != null) {
            tableRule.readPools = readTokenizedString(readPools, " ,");
        }

        String writePools = current.getAttribute("writePools");
        if (writePools != null) {
            tableRule.writePools = readTokenizedString(writePools, " ,");
        }

        NodeList children = current.getChildNodes();
        int childSize = children.getLength();

        for (int i = 0; i < childSize; i++) {
            Node childNode = children.item(i);

            if (childNode instanceof Element) {
                Element child = (Element) childNode;

                final String nodeName = child.getNodeName();
                if (nodeName.equals("rule")) {
                    tableRule.ruleList.add(loadRule(child, tableRule));
                }
            }
        }
        return tableRule;
    }

    private Rule loadRule(Element current, TableRule tableRule) throws InitialisationException {
        Rule rule = new Rule();

        // root
        rule.name = current.getAttribute("name");
        String group = current.getAttribute("group");
        rule.group = StringUtil.isEmpty(group) ? null : group;
        String ignoreArray = current.getAttribute("ignoreArray");
        rule.ignoreArray = Boolean.parseBoolean(ignoreArray);
        String isSwitch = current.getAttribute("isSwitch");
        rule.isSwitch = Boolean.parseBoolean(isSwitch);

        // parameters
        Element parametersNode = DocumentUtil.getTheOnlyElement(current, "parameters");
        if (parametersNode != null) {
            String[] tokens = readTokenizedString(parametersNode.getTextContent(), " ,");
            int index = 0;
            for (String parameter : tokens) {
                rule.parameterMap.put(parameter, index);
                Column column = new Column();
                column.setName(parameter);
                column.setTable(tableRule.table);
                rule.cloumnMap.put(column, index);
                index++;
            }

            tokens = readTokenizedString(parametersNode.getAttribute("excludes"), " ,");
            if (tokens != null) {
                for (String parameter : tokens) {
                    Column column = new Column();
                    column.setName(parameter);
                    column.setTable(tableRule.table);
                    rule.excludes.add(column);
                }
            }
        }

        // expression
        Element expression = DocumentUtil.getTheOnlyElement(current, "expression");
        rule.expression = expression.getTextContent();
        rule.rowJep = new RowJEP(rule.expression);
        try {
            rule.rowJep.parseExpression(rule.parameterMap, (Map<String, Variable>) null, this.ruleFunctionMap);
        } catch (com.meidusa.amoeba.sqljep.ParseException e) {
            throw new InitialisationException("parser expression:" + rule.expression + " error", e);
        }

        // defaultPools
        Element defaultPoolsNode = DocumentUtil.getTheOnlyElement(current, "defaultPools");
        if (defaultPoolsNode != null) {
            String defaultPools = defaultPoolsNode.getTextContent();
            rule.defaultPools = readTokenizedString(defaultPools, " ,");
        }

        // readPools
        Element readPoolsNode = DocumentUtil.getTheOnlyElement(current, "readPools");
        if (readPoolsNode != null) {
            rule.readPools = readTokenizedString(readPoolsNode.getTextContent(), " ,");
        }

        // writePools
        Element writePoolsNode = DocumentUtil.getTheOnlyElement(current, "writePools");
        if (writePoolsNode != null) {
            rule.writePools = readTokenizedString(writePoolsNode.getTextContent(), " ,");
        }

        return rule;
    }

    public static String[] readTokenizedString(String string, String delim) {
        return StringUtil.split(string, delim);
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

    public String getFunctionConfig() {
        return functionConfig;
    }

    public void setFunctionConfig(String functionConfig) {
        this.functionConfig = functionConfig;
    }

    public boolean isNeedParse() {
        return needParse;
    }

    public void setNeedParse(boolean needParse) {
        this.needParse = needParse;
    }

    public void setRuleFunctionConfig(String ruleFunctionConfig) {
        this.ruleFunctionConfig = ruleFunctionConfig;
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

    public Statment parseSql(DatabaseConnection connection, String sql) {
        Statment statment = null;

        String defaultSchema = (connection == null || StringUtil.isEmpty(connection.getSchema())) ? null : connection.getSchema();

        int sqlWithSchemaHashcode = defaultSchema != null ? (defaultSchema.hashCode() ^ sql.hashCode()) : sql.hashCode();
        mapLock.lock();
        try {
            statment = (Statment) map.get(sqlWithSchemaHashcode);
        } finally {
            mapLock.unlock();
        }
        if (statment == null) {
            synchronized (sql) {
                statment = (Statment) map.get(sqlWithSchemaHashcode);
                if (statment != null) return statment;

                Parser parser = newParser(sql);
                parser.setFunctionMap(this.functionMap);
                if (defaultSchema != null) {
                    Schema schema = new Schema();
                    schema.setName(defaultSchema);
                    parser.setDefaultSchema(schema);
                }
                try {

                    try {
                        statment = parser.doParse();
                        mapLock.lock();
                        try {
                            map.put(sqlWithSchemaHashcode, statment);
                        } finally {
                            mapLock.unlock();
                        }
                    } catch (Error e) {
                        logger.error(sql, e);
                        return null;
                    }

                } catch (ParseException e) {
                    logger.error(sql, e);
                }
            }
        }
        return statment;
    }

    public int parseParameterCount(DatabaseConnection connection, String sql) {
        Statment statment = parseSql(connection, sql);
        return statment.getParameterCount();
    }

}
