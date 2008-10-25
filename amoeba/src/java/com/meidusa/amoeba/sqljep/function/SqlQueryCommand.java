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

	@SuppressWarnings("unchecked")
	@Override
	public void evaluate(ASTFunNode node, JepRuntime runtime)
			throws ParseException {
		
		node.childrenAccept(runtime.ev, null);
		
		Comparable<?>[] parameters = null;
		if(parameterSize >1){
			parameters = new Comparable<?>[parameterSize-1] ;
			for(int i=0;i<parameters.length;i++){
				parameters[i] = runtime.stack.pop();
			}
		}
		
		String returnColumnName = null;
		Comparable<?> firstParameter = runtime.stack.pop();
		if(firstParameter instanceof Comparative){
			Comparable<?> value= ((Comparative)firstParameter).getValue();
			returnColumnName = value!= null ? value.toString():null;
		}else{
			returnColumnName = firstParameter.toString();
		}
		
		Map<String,Object> result = null;
		if(isThreadLocalCache()){
			
			int threadLocalKey = this.hashCode();
			if(parameterSize>1){
				int hash = this.hashCode();
				for(int i=0;i<parameters.length;i++){
					if(parameters[i] instanceof Comparative){
						Comparative comp = (Comparative)parameters[i];
						if(comp != null && comp.getValue() != null){
							hash ^= comp.getValue().hashCode() << i;
						}
					}
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
			runtime.stack.push(null);	
		}else{
			runtime.stack.push((Comparable<?>)result.get(returnColumnName));
		}
		
		
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
					Object columnValue = rs.getObject(i);
					columnMap.put(columnName, columnValue);
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
}
