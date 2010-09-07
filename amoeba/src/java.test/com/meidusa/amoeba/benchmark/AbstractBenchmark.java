package com.meidusa.amoeba.benchmark;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.meidusa.amoeba.net.MultiConnectionManagerWrapper;
import com.meidusa.amoeba.util.InitialisationException;

public abstract class AbstractBenchmark {
	private static AbstractBenchmark benckmark;
	protected static void setBenchmark(AbstractBenchmark benckmark){
		AbstractBenchmark.benckmark = benckmark;
	}
	public static AbstractBenchmark getInstance(){
		return AbstractBenchmark.benckmark;
	}
	public abstract AbstractBenchmarkClientConnection<?> newBenchmarkClientConnection(SocketChannel channel,long time,CountDownLatch latcher);
	
	public static void main(String[] args) throws IOException,
			InterruptedException, InitialisationException {
		if(args != null && args.length ==1 && "-h".equalsIgnoreCase(args[0])){
			System.out.println("-Dconn=<int> ;Concurrency connection size\r\n");
			System.out.println("-Dip=<String> ;remote ip\r\n");
			System.out.println("-Dport=<int> ;remote port\r\n");
			System.out.println("-Dtotle=<int> ;totle request\r\n");
			System.out.println("-Ddebug=<Boolean> ;enable debug\r\n");
			return;
		}

		int conn = Integer.parseInt(System.getProperty("conn", "100"));
		final int totle = Integer.parseInt(System.getProperty("totle", "1000"));
		String ip = System.getProperty("ip", "127.0.0.1");
		final CountDownLatch latcher = new CountDownLatch(totle);
		
		int port = Integer.parseInt(System.getProperty("port", "8066"));
		
		MultiConnectionManagerWrapper manager = new MultiConnectionManagerWrapper();
		manager.init();
		manager.start();
		
		new Thread(){
			long lastCount = latcher.getCount();
			{this.setDaemon(true);}
			public void run(){
				while(latcher.getCount()>0){
					long current = latcher.getCount();
					long tps = lastCount - current;
					lastCount = current;
					System.out.println(new Date() +"     compeleted="+(totle - lastCount)+ " TPS="+tps);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				System.out.println(new Date() +"     compeleted="+(totle));
			}
			
		}.start();
		
		System.out.println("\r\nconnect to ip="+ip+",port="+port+",connection size="+conn+",totle request="+totle);
		AbstractBenchmark benckmark = AbstractBenchmark.getInstance();
		List<AbstractBenchmarkClientConnection<?>> connList = new ArrayList<AbstractBenchmarkClientConnection<?>>();
		for(int i=0;i<conn;i++){
			AbstractBenchmarkClientConnection<?> connection = benckmark.newBenchmarkClientConnection(SocketChannel.open(new InetSocketAddress(ip,port)),System.currentTimeMillis(),latcher);
			manager.postRegisterNetEventHandler(connection, SelectionKey.OP_READ);
			connList.add(connection);
		}
		
		
		for(AbstractBenchmarkClientConnection<?> connection: connList){
			connection.postMessage(connection.createRequestPacket().toByteBuffer(connection));
		}
		latcher.await();
		
		long min = connList.get(0).min;
		long max = 0;
		long average = 0;
		long cost = 0;
		long count = 0;
		long minStart = connList.get(0).start;
		long maxend = 0;
		for(AbstractBenchmarkClientConnection<?> connection: connList){
			if(connection.count>0){
				min = Math.min(min, connection.min);
				max = Math.max(max, connection.max);
				cost += (connection.end - connection.start);
				count += connection.count;
				minStart = Math.min(minStart,connection.start);
				maxend = Math.max(maxend, connection.end);
			}
		}
		average = cost / totle;
		long time = TimeUnit.MILLISECONDS.convert((maxend - minStart),TimeUnit.NANOSECONDS);
		System.out.println("completed requests totle="+totle+", cost="+TimeUnit.MILLISECONDS.convert((maxend - minStart), TimeUnit.NANOSECONDS)+"ms , TPS="+ (time>0?((long)totle*1000)/time:totle)+"/s");
		System.out.println("min="+TimeUnit.MILLISECONDS.convert(min, TimeUnit.NANOSECONDS)+"ms");
		System.out.println("max="+TimeUnit.MILLISECONDS.convert(max, TimeUnit.NANOSECONDS)+"ms");
		System.out.println("average="+TimeUnit.MILLISECONDS.convert(average, TimeUnit.NANOSECONDS)+"ms");
		manager.shutdown();
	}
}
