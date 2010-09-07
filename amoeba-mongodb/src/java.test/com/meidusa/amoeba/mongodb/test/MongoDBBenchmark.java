package com.meidusa.amoeba.mongodb.test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.BasicBSONObject;

import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.packet.DeleteMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.GetMoreMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.InsertMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.KillCurosorsMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MessageMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.UpdateMongodbPacket;
import com.meidusa.amoeba.net.MultiConnectionManagerWrapper;
import com.meidusa.amoeba.net.packet.Packet;
import com.meidusa.amoeba.util.InitialisationException;

public class MongoDBBenchmark {

	public static void main(String[] args) throws IOException,
			InterruptedException, InitialisationException {
		if(args != null && args.length ==1 && "-h".equalsIgnoreCase(args[0])){
			System.out.println("-Dconn=<int> ;Concurrency connection size\n");
			System.out.println("-Dip=<String> ;remote ip\n");
			System.out.println("-Dport=<int> ;remote port\n");
			System.out.println("-Dtotle=<int> ;totle request\n");
			System.out.println("-Ddebug=<Boolean> ;enable debug\n");
			System.out.println("-DrequestFile=<File> ;request packet parameter file\n");
			return;
		}

		int conn = Integer.parseInt(System.getProperty("conn", "100"));
		final int totle = Integer.parseInt(System.getProperty("totle", "1000"));
		String ip = System.getProperty("ip", "127.0.0.1");
		final CountDownLatch latcher = new CountDownLatch(totle);
		
		final boolean debug = Boolean.getBoolean("debug");
		String file = System.getProperty("requestFile", "packet.request");
		int port = Integer.parseInt(System.getProperty("port", "8066"));
		
		MultiConnectionManagerWrapper manager = new MultiConnectionManagerWrapper();
		manager.init();
		manager.start();
		
		/*final Properties properties = new Properties();
		properties.load(new FileInputStream(new File(file)));
*/		
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
		
		class TestClientConnection extends ClientConnection {
			private AtomicInteger index = new AtomicInteger();
			long min = System.nanoTime();
			long start = 0;
			long max = 0;
			long end = min;
			long next = min;
			long count = 0;
			boolean isWorked = false;
			private Random random = new Random();
			public TestClientConnection(SocketChannel channel, long createStamp) {
				super(channel, createStamp);
				start = System.nanoTime();
			}
			
			public QueryMongodbPacket createPacket(){
				QueryMongodbPacket packet = new QueryMongodbPacket();
				packet.fullCollectionName = "test.test";
				packet.numberToReturn = 10000;
				packet.returnFieldSelector = new BasicBSONObject();
				packet.returnFieldSelector.put("f", 1);
				packet.numberToSkip = 0;
				packet.requestID = index.getAndIncrement();
				BasicBSONObject query = new BasicBSONObject();
				query.put("s", random.nextInt(400));
				packet.query = query;
				return packet;
			}
			protected void doReceiveMessage(byte[] message) {
				latcher.countDown();
				end = System.nanoTime();
				long current = end - next;
				min = Math.min(min, current);
				max = Math.max(max, current);
				count ++;
				int type = MongodbPacketBuffer.getOPMessageType(message);
				if (type == MongodbPacketConstant.OP_REPLY) {
					if(debug){
						ResponseMongodbPacket packet = new ResponseMongodbPacket();
						packet.init(message,this);
						System.out.println("<--"+packet);
					}
				}
				
				if(latcher.getCount()<=0){
					return;
				}
				postMessage(createPacket().toByteBuffer(this));
			}
			public void postMessage(ByteBuffer msg) {
				next = System.nanoTime();
				if(debug){
					int type = MongodbPacketBuffer.getOPMessageType(msg.array());
					Packet packet = null;
					switch(type){
					case MongodbPacketConstant.OP_QUERY:
						packet = new QueryMongodbPacket();
						break;
					case MongodbPacketConstant.OP_GET_MORE:
						packet = new GetMoreMongodbPacket();
						break;
					case MongodbPacketConstant.OP_DELETE:
						packet = new DeleteMongodbPacket();
						break;
					case MongodbPacketConstant.OP_KILL_CURSORS:
						packet = new KillCurosorsMongodbPacket();
						break;
					case MongodbPacketConstant.OP_UPDATE:
						packet = new UpdateMongodbPacket();
						break;
					case MongodbPacketConstant.OP_INSERT:
						packet = new InsertMongodbPacket();
						break;
					case MongodbPacketConstant.OP_MSG:
						packet = new MessageMongodbPacket();
						break;
					}
					packet.init(msg.array(), this);
					System.out.println("--->"+packet);
				}
				super.postMessage(msg);
				
			}
			
		};
		
		List<TestClientConnection> connList = new ArrayList<TestClientConnection>();
		for(int i=0;i<conn;i++){
			TestClientConnection connection = new TestClientConnection(SocketChannel.open(new InetSocketAddress(ip,port)),System.currentTimeMillis());
			manager.postRegisterNetEventHandler(connection, SelectionKey.OP_READ);
			connList.add(connection);
		}
		
		for(TestClientConnection connection: connList){
			connection.postMessage(connection.createPacket().toByteBuffer(connection));
		}
		latcher.await();
		
		long min = connList.get(0).min;
		long max = 0;
		long average = 0;
		long cost = 0;
		long count = 0;
		long minStart = connList.get(0).start;
		long maxend = 0;
		for(TestClientConnection connection: connList){
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
	protected static Packet getPacket(Properties properties){
		QueryMongodbPacket packet = new QueryMongodbPacket();
		packet.fullCollectionName = "test.test";
		packet.numberToReturn = 100;
		packet.numberToSkip = 0;
		//packet.requestID = index.getAndIncrement();
		BasicBSONObject query = new BasicBSONObject();
		Random random = new Random(400);
		query.put("s", random.nextInt());
		packet.query = new BasicBSONObject();
		
		final Map<String ,String > parameterMap = new HashMap<String,String>(); 
		final Map<String ,Object > beanParameterMap = new HashMap<String,Object>(); 
		for(Map.Entry<Object,Object> entry : properties.entrySet()){
			if(entry.getKey().toString().startsWith("parameterMap.")){
				parameterMap.put(entry.getKey().toString().substring("parameterMap.".length()), entry.getValue().toString());
			}else{
				beanParameterMap.put(entry.getKey().toString(), entry.getValue());
			}
		}
		
		ParameterMapping.mappingObjectField(packet, beanParameterMap,Object.class);
		//packet.parameterMap = parameterMap;
		
		return packet;
	}
	
}
