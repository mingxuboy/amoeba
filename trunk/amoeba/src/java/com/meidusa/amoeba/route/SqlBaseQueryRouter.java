package com.meidusa.amoeba.route;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.dbobject.Schema;
import com.meidusa.amoeba.parser.dbobject.Table;
import com.meidusa.amoeba.parser.function.LastInsertId;
import com.meidusa.amoeba.parser.statement.AbstractStatement;
import com.meidusa.amoeba.parser.statement.DMLStatement;
import com.meidusa.amoeba.parser.statement.PropertyStatement;
import com.meidusa.amoeba.parser.statement.SelectStatement;
import com.meidusa.amoeba.parser.statement.ShowStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.sqljep.function.Comparative;
import com.meidusa.amoeba.util.StringUtil;
import com.meidusa.amoeba.util.ThreadLocalMap;

public abstract class SqlBaseQueryRouter extends AbstractQueryRouter<DatabaseConnection,SqlQueryObject> {

    private Lock                                    mapLock         = new ReentrantLock(false);
    
    private boolean replaceEscapeSymbol = false;
    
    protected void beforeSelectPool(DatabaseConnection connection, SqlQueryObject queryObject){
    	Statement statment = parseStatement(connection,queryObject.sql);
		if(statment instanceof DMLStatement){
			DMLStatement dmlStatment = ((DMLStatement)statment);
			queryObject.isRead = dmlStatment.isReadStatement();
		}
		ThreadLocalMap.put(_CURRENT_QUERY_OBJECT_, queryObject);
    }

	@Override
	protected Map<Table, Map<Column, Comparative>> evaluateTable(DatabaseConnection connection,SqlQueryObject queryObject) {
		Statement statment = parseStatement(connection,queryObject.sql);
		Map<Table, Map<Column, Comparative>> tables = null;
		if(statment instanceof DMLStatement){
			tables = ((DMLStatement)statment).evaluate(queryObject.parameters);
			return tables;
		}else if (statment instanceof PropertyStatement) {
			if (logger.isDebugEnabled()) {
                logger.debug("ShowStatment:[" + queryObject.sql + "]");
            }
            setProperty(connection, (PropertyStatement) statment, queryObject);
            return null;
        }else if (statment instanceof ShowStatement) {
            if (logger.isDebugEnabled()) {
                logger.debug("ShowStatment:[" + queryObject.sql + "]");
            }
            AbstractStatement ast = (ShowStatement)statment;
            if(ast.getTables() != null){
            	tables = new HashMap<Table, Map<Column, Comparative>>();
	            for(Table table:ast.getTables()){
	            	tables.put(table, null);
	            }
            }else{
            	tables.put(null, null);
            }
            
            return tables;
        }
		return null;
	}
	
	protected String amoebaRouterSql(String sql){
		sql = sql.trim();
		
		int cIndex = sql.indexOf("/*");
		int sIndex = sql.indexOf("@amoeba");
		if(sIndex >0 && sIndex > cIndex){
			String subSql = sql.substring(sIndex);
			int lIndex = subSql.indexOf("*/");
			if(lIndex>0 ){
				subSql = subSql.substring(0,lIndex);
				int pIndex = subSql.lastIndexOf("]");
				
				lIndex = subSql.lastIndexOf(")");
				if(pIndex < lIndex){
					subSql = subSql.substring(0,lIndex+1);
				}else{
					subSql = subSql.substring(0,pIndex+1) +" " + sql ; 
				}
				sql = subSql;
			}
		}
		
		sql = sql.trim();
			
		
		if(replaceEscapeSymbol){
			String s = new String(new char[]{(char)0x5c,(char)0x5c});
			sql = StringUtil.replace(sql,s,"");
			s = new String(new char[]{(char)0x5c,(char)0x27});
			sql = StringUtil.replace(sql,s,"");
		}
		return sql;
	}
	
	public Statement parseStatement(DatabaseConnection connection, String sql) {
        Statement statment = null;
        String defaultSchema = (connection == null || StringUtil.isEmpty(connection.getSchema())) ? null : connection.getSchema();

        long sqlKey = ((long) sql.length() << 32) | (long) (defaultSchema != null ? (defaultSchema.hashCode() ^ sql.hashCode()) : sql.hashCode());
        mapLock.lock();
        try {
            statment = (Statement) map.get(sqlKey);
        } finally {
            mapLock.unlock();
        }
        if (statment == null) {
            synchronized (sql) {
                statment = (Statement) map.get(sqlKey);
                if (statment != null) {
                    return statment;
                }

                Parser parser = newParser(amoebaRouterSql(sql));
                parser.setFunctionMap(this.functionMap);
                if (defaultSchema != null) {
                    Schema schema = new Schema();
                    schema.setName(defaultSchema);
                    parser.setDefaultSchema(schema);
                }

                try {
                    statment = parser.doParse();
                    if(statment instanceof SelectStatement){
                    	SelectStatement st = (SelectStatement)statment;
                    	if(st.getTables() == null || st.getTables().length == 0){
                    		Boolean queryInsertId = (Boolean)ThreadLocalMap.get(LastInsertId.class.getName());
                    		if(queryInsertId != null && queryInsertId.booleanValue()){
                    			st.setQueryLastInsertId(true);
                    		}
                    	}
                    }
                    mapLock.lock();
                    if(statment instanceof DMLStatement){
                    	((DMLStatement)statment).setSql(sql);
                    }
                    try {
                        map.put(sqlKey, statment);
                    } finally {
                        mapLock.unlock();
                    }
                } catch (Error e) {
                    logger.error(sql, e);
                    return null;
                }catch(Exception e){
                	logger.error(sql, e);
                    return null;
                }
               
            }
        }
        return statment;
    }
	
	protected void setProperty(DatabaseConnection connection,
			Statement statment, SqlQueryObject queryObject) {
		
	}

	 public int parseParameterCount(DatabaseConnection connection, String sql) {
		Statement statment = parseStatement(connection, sql);
		if (statment != null) {
			return statment.getParameterCount();
		} else {
			return 0;
		}
	}
	 
	public abstract Parser newParser(String sql);

}
