package com.meidusa.amoeba.mysql.test.jdbc;

import java.sql.Connection;
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
        	for(int i=0;i<1;i++){
            statment = conn.prepareStatement("SELECT ID, SDID, F_SDID, APP_ID, RESERVE1, RESERVE2, RESERVE3 FROM SD_RELATION.RELATION_ORIGIN WHERE SDID =? AND F_SDID=? AND APP_ID = ?");
            statment.setLong(1, 111111);
            statment.setLong(2, 123);
            statment.setLong(3, 112);
            ResultSet rs = statment.executeQuery();
            statment.getResultSet();
            while(rs.next()){
            	System.out.println(rs.getString("ID"));
            }
           if (statment != null) {
               try {
                   statment.close();
               } catch (Exception e) {
               }
           }
          /* statment = conn.prepareStatement("select LAST_INSERT_ID() as id");
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
           }*/
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
