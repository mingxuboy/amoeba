package com.meidusa.amoeba.shutdown;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.meidusa.amoeba.config.ConfigUtil;

public class ShutdownClient extends ShutdownRunner{

	public ShutdownClient(String appplicationName) {
		super(appplicationName);
	}

	public void run() {

		InetAddress address = null;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					socketInfoFile));
			String port = reader.readLine();
			Socket socket = new Socket(address, Integer.parseInt(port));
			
			ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
			os.writeObject(Command.SHUTDOWN);
			ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
			if(is.readObject() == Command.OK){
				System.out.println("remote application= "+ appplicationName+":"+port+" shutdown completed");
			}
			socketInfoFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File socketId = new File(ConfigUtil.filter("${amoeba.home}"),"amoeba.pid");
		ShutdownClient client = new ShutdownClient(socketId.getAbsolutePath());
		client.init();
		client.run();
	}

}
