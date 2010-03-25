package com.meidusa.amoeba.mysql.test.jdbc;

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

        // ∆Ù”√failover π ’œª÷∏¥π¶ƒ‹
        // props.put("autoReconnect", "false");

        // ∆Ù”√¬÷—Ø
        // props.put("roundRobinLoadBalance", "false");

        props.put("user", "root");
        // props.put("password", "....");
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = null;
        PreparedStatement statment = null;
        ResultSet result = null;
        conn = DriverManager.getConnection("jdbc:mysql://114.80.135.7:8066/test?useUnicode=true&characterEncoding=gbk", "sdfriend", "sdfriend");
        try {
            statment = conn.prepareStatement("insert into aaaa(name) values('asdfasdf')");

           int id = statment.executeUpdate();
           System.out.println(id);
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

            if (conn != null) {
                conn.close();
            }
        }
    }
}
