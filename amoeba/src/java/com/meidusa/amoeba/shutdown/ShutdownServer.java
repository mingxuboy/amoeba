package com.meidusa.amoeba.shutdown;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import com.meidusa.amoeba.config.ConfigUtil;

public class ShutdownServer extends ShutdownRunner{

	public ShutdownServer(String appplicationName) {
		super(appplicationName);
	}

	public void run() {
		Random random = new Random();
		InetAddress address = null;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
		ServerSocket server = null;
		int times = 0;
		do{
		try {
			int port = random.nextInt(65535); 
			times ++;
			server = new ServerSocket(port,50,address);
			FileWriter writer = new FileWriter(socketInfoFile);
			writer.write(""+port);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			if(times >100){
				System.out.println("cannot create shutdownServer socket,System exit now!");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		}while(server == null);
		final ServerSocket finalServer = server;
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				try {
					finalServer.close();
				} catch (IOException e) {
				}
			}
		});
		while(true){
			try {
				final Socket socket = finalServer.accept();
				socket.setSoTimeout(1000);
				new Thread(){
					public void run(){
						try {
							ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
							Command command = (Command)is.readObject();
							if(command == Command.SHUTDOWN){
								System.out.println("application shutdown now ...");
								ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
								os.writeObject(Command.OK);
								System.exit(0);
							}else{
								socket.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			} catch (IOException e) {
			}
		}
		
	}
	
	public static void main(String[] args){
		File socketId = new File(ConfigUtil.filter("${amoeba.home}"),"amoeba.pid");
		ShutdownServer server = new ShutdownServer(socketId.getAbsolutePath());
		server.init();
		server.run();
	}
}
