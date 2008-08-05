package com.meidusa.amoeba.mysql.jdbc;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PerformaceTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		int threadCount = 400;
		int totleQuery = 250;
		int count = 10;
		if(args.length>=2){
			threadCount = Integer.parseInt(args[0]);
			totleQuery = Integer.parseInt(args[1]);
			if(args.length>2){
				count = Integer.parseInt(args[2]);
			}
		}
		final int runcount = count;
		Properties props = new Properties(); 

		// ∆Ù”√failover π ’œª÷∏¥π¶ƒ‹
		//props.put("autoReconnect", "false"); 

		//∆Ù”√¬÷—Ø
		//props.put("roundRobinLoadBalance", "false"); 

		props.put("user", "root"); 
		//props.put("password", "...."); 
		int testCount = totleQuery;
		Class.forName("com.mysql.jdbc.Driver");
		ThreadPoolExecutor executor = new ThreadPoolExecutor(threadCount, threadCount, Long.MAX_VALUE, TimeUnit.NANOSECONDS,new LinkedBlockingQueue<Runnable>());
		long start = System.currentTimeMillis();
		final CountDownLatch latch = new CountDownLatch(testCount);
		for(int i=0;i<testCount;i++){
			executor.execute(new Runnable(){

				public void run() {
					Connection conn = null;
					PreparedStatement statment = null;
					ResultSet result = null;
					try{
						conn = DriverManager.getConnection("jdbc:mysql://10.0.38.105:3306/test","root",null);
						for(int i=0;i<runcount;i++){
							try{
							statment = conn.prepareStatement("SELECT * FROM promotion.choice_option");
							result = statment.executeQuery();
							}catch(Exception e){
								e.printStackTrace();
							}finally{
								if(result != null){
									try{
										result.close();
									}catch(Exception e){}
								}
								
								if(statment != null){
									try{
										statment.close();
									}catch(Exception e){}
								}
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						if(conn != null){
							try{
								conn.close();
							}catch(Exception e){}
						}
						latch.countDown();
						System.out.println("countDown count:"+latch.getCount());
					}
					
				}
			});
		}
		latch.await();
		long end = System.currentTimeMillis();
		System.out.println("totle:"+(end-start));
		executor.shutdownNow();
	}
}
