package com.meidusa.amoeba.mysql.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class PreparedStatmentTest {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Properties props = new Properties();

		// 启用failover 故障恢复功能
		// props.put("autoReconnect", "false");

		// 启用轮询
		// props.put("roundRobinLoadBalance", "false");

		props.put("user", "root");
		// props.put("password", "....");
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = null;
		PreparedStatement statment = null;
		ResultSet result = null;
		conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:8066/test",
				"root", null);
		try {
			statment = conn
					.prepareStatement("update t_qa_question set question_Id=?, title=?, content=?, extend_content=?, category=?, ask_time=?, finish_time=?, user_id=?,"+
							"offer_score=?, had_append=?, append_score=?, total_score=?, total_gold=?, dead_line=?, is_urgent=?, " +
							
							"best_answer=?, anonymous=?, state=?,flowers=?, eggs=?, click=? where id=?");
			statment.setString(1, "hello admoeba");
			statment.setString(2, "阿瑟多幅");
			statment.setString(3, "asdfqwerqwer阿瑟多幅第三方");
			statment.setString(4, "11111111111111111qwerqwer阿瑟多幅第三方");
			//category
			statment.setInt(5, 11);
			statment.setDate(6, new Date(System.currentTimeMillis()));
			statment.setDate(7, new Date(System.currentTimeMillis()));
			statment.setInt(8, 11111);
			
			//offer_score
			statment.setInt(9, 112);
			statment.setString(10, "setStringset");
			statment.setInt(11, 111);
			statment.setInt(12, 11234);
			statment.setInt(13, 123456);
			
			//dead_line
			statment.setDate(14, new Date(System.currentTimeMillis()));
			
			statment.setString(15, "setStringsetStdf");
			
			//best_answer
			statment.setInt(16, 111111111);
			statment.setString(17, "Y");
			statment.setInt(18, 11234);
			statment.setInt(19, 11234);
			statment.setInt(20, 11234);
			statment.setInt(21, 11234);
			
			//id
			statment.setInt(22, 1);
			
			int count = statment.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (Exception e) {
				}
			}

			if (statment != null) {
				try {
					statment.close();
				} catch (Exception e) {
				}
			}
			
			if(conn != null){
				conn.close();
			}
		}
	}
}
