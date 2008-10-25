package com.meidusa.amoeba.sqljep.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.ParseException;
import com.meidusa.amoeba.util.ThreadLocalMap;

/**
 * 请使用@see JdbcConnectionFactory 提供的pool
 * @author struct
 *
 */
public class SqlQueryCommand extends PostfixCommand{
	private static Logger logger = Logger.getLogger(SqlQueryCommand.class);
	private String sql;
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	private String poolName;
	private int parameterSize;
	private String returnColumnName;
	
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

	public void setReturnColumnName(String returnColumnName) {
		this.returnColumnName = returnColumnName;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setParameterSize(int parameterSize) {
		this.parameterSize = parameterSize;
	}

	@Override
	public void evaluate(ASTFunNode node, JepRuntime runtime)
			throws ParseException {
		
		node.childrenAccept(runtime.ev, null);
		
		Comparable<?>[] parameters = new Comparable<?>[getNumberOfParameters()] ;
		for(int i=0;i<parameters.length;i++){
			parameters[i] = runtime.stack.pop();
		}
		
		
		Comparable<?> result = null;
		if(isThreadLocalCache()){
			
			int threadLocalKey = this.hashCode();
			if(getNumberOfParameters() != 0){
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
			
			result = (Comparable<?>)ThreadLocalMap.get(threadLocalKey);
			if(result == null){
				if(!ThreadLocalMap.containsKey(threadLocalKey)){
					result = query(parameters,returnColumnName);
					ThreadLocalMap.put(threadLocalKey,result);
				}
			}
		}else{
			result = query(parameters,returnColumnName);
		}
		runtime.stack.push(result);	
		
	}

	private Comparable<?> query(Comparable<?>[] parameters,String returnColumnName){
		ObjectPool pool =  ProxyRuntimeContext.getInstance().getPoolMap().get(poolName);
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try{
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
			if(rs.next()){
				return (Comparable<?>)rs.getObject(returnColumnName);
			}
			return null;
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
}
