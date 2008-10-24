package com.meidusa.amoeba.sqljep.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.ParseException;

/**
 * 请使用@see JdbcConnectionFactory 提供的pool
 * @author struct
 *
 */
public class SqlQueryCommand extends PostfixCommand{
	private String sql;
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	private String poolName;
	private int parameterSize;
	private String returnColumnName;
	
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
					}
				}
			}
			
			rs = st.executeQuery();
			if(rs.next()){
				runtime.stack.push((Comparable<?>)rs.getObject(returnColumnName));	
				return ;
			}
		}catch(Exception e){
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
