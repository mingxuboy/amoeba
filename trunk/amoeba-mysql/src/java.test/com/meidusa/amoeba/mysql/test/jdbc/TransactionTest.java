package com.meidusa.amoeba.mysql.test.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;

public class TransactionTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test?user=blog&password=blog&useUnicode=true");
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		/*conn.setAutoCommit(false);
		PreparedStatement statment = conn.prepareStatement("select * from account where id > ? and create_time > ?");
		statment.setInt(1,1);
		statment.setDate(2, new Date(Calendar.getInstance().getTime().getTime()));
		
		ResultSet result = statment.executeQuery();
		while(!result.isLast()){
			System.out.println(result.next());
		}
		conn.commit();
		conn.rollback();
		conn.setAutoCommit(true);*/
		conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		
	}

}
