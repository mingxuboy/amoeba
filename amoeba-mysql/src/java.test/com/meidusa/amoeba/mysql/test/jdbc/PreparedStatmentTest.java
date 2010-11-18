package com.meidusa.amoeba.mysql.test.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class PreparedStatmentTest {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		for (int j = 0; j < 1; j++) {
			Thread thread =new Thread() {
				public void run() {
					Connection conn = null;
					PreparedStatement statment = null;
					ResultSet result = null;
					try {
						Properties props = new Properties();

						// ÆôÓÃfailover ¹ÊÕÏ»Ö¸´¹¦ÄÜ
						// props.put("autoReconnect", "false");

						// ÆôÓÃÂÖÑ¯
						// props.put("roundRobinLoadBalance", "false");

						props.put("user", "root");
						// props.put("password", "....");
						try {
							Class.forName("com.mysql.jdbc.Driver");
						} catch (ClassNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						conn = DriverManager
								.getConnection(
										"jdbc:mysql://127.0.0.1:8066/test?useUnicode=true&characterEncoding=utf-8&useServerPrepStmts=true&useCompression=true",
										"root", null);
						for (int i = 0; i < 1; i++) {
							statment = conn
									.prepareStatement("SELECT ID, SDID, F_SDID, APP_ID, RESERVE1, RESERVE2, RESERVE3 FROM SD_RELATION.RELATION_ORIGIN WHERE SDID =? AND F_SDID=? AND APP_ID = ?");
							statment.setLong(1, 1);
							statment.setLong(2, 2);
							statment.setLong(3, 1);
							ResultSet rs = statment.executeQuery();
							statment.getResultSet();
							while (rs.next()) {
								System.out.println(rs.getString("ID"));
							}

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

							/*
							 * statment =
							 * conn.prepareStatement("select LAST_INSERT_ID() as id"
							 * ); result = statment.executeQuery();
							 * result.next(); Object lastInsertId =
							 * result.getLong("id");
							 * System.out.println(lastInsertId); if (result !=
							 * null) { try { result.close(); } catch (Exception
							 * e) { } } if (statment != null) { try {
							 * statment.close(); } catch (Exception e) { } }
							 * 
							 * statment =conn.prepareStatement(
							 * "select * from SD_RELATION.RELATION_ORIGIN where sdid=35676 and f_sdid=129"
							 * +i); result = statment.executeQuery(); if(
							 * result.next()) lastInsertId =
							 * result.getLong("id"); if (result != null) { try {
							 * result.close(); } catch (Exception e) { } } if
							 * (statment != null) { try { statment.close(); }
							 * catch (Exception e) { } }
							 */
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (conn != null) {
							try {
								conn.close();
							} catch (SQLException e) {
							}
						}
					}
				}
			};
			thread.join();
			thread.start();
		}

	}
}
