package com.meidusa.amoeba.sqljep.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.ParseException;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.StringUtil;
import com.meidusa.amoeba.util.ThreadLocalMap;

/**
 * 请使用@see JdbcConnectionFactory 提供的pool
 * @author struct
 *
 */
public class SqlQueryCommand extends PostfixCommand implements Initialisable{
	private static Logger logger = Logger.getLogger(SqlQueryCommand.class);
	private String sql;
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	private String poolName;
	private int parameterSize=1;
	
	/**
	 * enable threadlocal cache or not
	 */
	private boolean threadLocalCache = false;
	public boolean isThreadLocalCache() {
		return threadLocalCache;
	}

	public void setThreadLocalCache(boolean threadLocalCache) {
		this.threadLocalCache = threadLocalCache;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	@Override
	public Comparable<?>[] evaluate(ASTFunNode node, JepRuntime runtime)
			throws ParseException {
		
		node.childrenAccept(runtime.ev, null);
		
		Comparable<?>[] parameters = null;
		parameters = new Comparable<?>[parameterSize] ;
		for(int i=parameterSize-1;i>=0;i--){
			parameters[i] = runtime.stack.pop();
		}
		return parameters;
	}

	private Map<String,Object> query(Comparable<?>[] parameters){
		ObjectPool pool =  ProxyRuntimeContext.getInstance().getPoolMap().get(poolName);
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try{
			Map<String,Object> columnMap = null;
			conn = (Connection)pool.borrowObject();
			st = conn.prepareStatement(sql);
			if(parameters != null){
				for(int i=0;i<parameters.length;i++){
					if(parameters[i] instanceof Comparative){
						st.setObject(i+1, ((Comparative)parameters[i]).getValue());
					}else{
						st.setObject(i+1, parameters[i]);
					}
				}
			}
			
			rs = st.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			if(rs.next()){
				columnMap= new HashMap<String,Object>();
				for(int i=1;i<=metaData.getColumnCount();i++){
					String columnName = metaData.getColumnName(i);
					String label = metaData.getColumnLabel(i);
					Object columnValue = rs.getObject(i);
					if(label!= null && !StringUtil.equalsIgnoreCase(label, columnName)){
						columnMap.put(label.toLowerCase(), columnValue);
					}
					columnMap.put(columnName.toLowerCase(), columnValue);
				}
			}
			return columnMap;
		}catch(Exception e){
			logger.error("execute sql error :"+sql,e);
			return null;
		}finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e1) {
				}
			}
			
			if(st != null){
				try {
					st.close();
				} catch (SQLException e1) {
				}
			}
			
			if(conn != null){
				try {
					pool.returnObject(conn);
				} catch (Exception e) {
				}
			}
			
		}
	}
	@Override
	public int getNumberOfParameters() {
		return parameterSize;
	}

	public void init() throws InitialisationException {
		parameterSize = ProxyRuntimeContext.getInstance().getQueryRouter().parseParameterCount(null, sql)+1;
	}

	@SuppressWarnings("unchecked")
	public Comparable<?> getResult(Comparable<?>... comparables)
			throws ParseException {
		String returnColumnName = null;
		returnColumnName = comparables[0].toString().toLowerCase();
		
		Map<String,Object> result = null;
		Comparable<?>[] parameters = new Comparable<?>[comparables.length-1];
		if(isThreadLocalCache()){
			
			int threadLocalKey = this.hashCode();
			if(parameterSize>1){
				int hash = this.hashCode();
				for(int i=0;i<parameters.length;i++){
					parameters[i] = comparables[i+1];
					hash ^= parameters[i].hashCode() << (i+1);
				}
				threadLocalKey = threadLocalKey ^ hash;
			}
			
			result = (Map<String,Object>)ThreadLocalMap.get(threadLocalKey);
			if(result == null){
				if(!ThreadLocalMap.containsKey(threadLocalKey)){
					result = query(parameters);
					ThreadLocalMap.put(threadLocalKey,result);
				}
			}
		}else{
			result = query(parameters);
		}
		if(result == null){
			return (null);	
		}else{
			return ((Comparable<?>)result.get(returnColumnName));
		}
	}
}
