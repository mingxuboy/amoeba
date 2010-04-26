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
        
        conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:8066/test?useUnicode=true&characterEncoding=utf-8&useServerPrepStmts=true", "sdfriend", "sdfriend");
        try {
        	for(int i=2800;i<2900;i++){
            statment = conn.prepareStatement("select ID, TOPIC_ID, CREATE_TIME, TOPIC_CONTENT,LAST_MESSAGE_TIME,MESSAGE_COUNT  from SD_MESSAGE.TOPIC_CONTENT   where TOPIC_ID = ?");
            statment.setString(1, "bf016b7153444c228b8b0bdc3096f67e");
            ResultSet rs = statment.executeQuery();
            while(rs.next()){
            	System.out.println(rs.getString("TOPIC_CONTENT"));
            }
           if (statment != null) {
               try {
                   statment.close();
               } catch (Exception e) {
               }
           }
           statment = conn.prepareStatement("select LAST_INSERT_ID() as id");
           result = statment.executeQuery();
           result.next();
           Object lastInsertId = result.getLong("id");
           System.out.println(lastInsertId);
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
           
           statment = conn.prepareStatement("select * from SD_RELATION.RELATION_ORIGIN where sdid=35676 and f_sdid=129"+i);
           result = statment.executeQuery();
          if( result.next())
           lastInsertId = result.getLong("id");
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
        }
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
