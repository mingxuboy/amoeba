package com.meidusa.amoeba.monitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Random;

import org.apache.log4j.Level;

import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.net.ServerableConnectionManager;
import com.meidusa.amoeba.util.InitialisationException;

public class MonitorServer extends ServerableConnectionManager{

	protected File socketInfoFile;
	protected String appplicationName;
	public void init() throws InitialisationException {
		super.init();
		socketInfoFile = new File(ConfigUtil.filter("${amoeba.home}"),appplicationName+".shutdown.port");
	}
	
	public MonitorServer(String appplicationName) throws IOException {
		super();
		this.setName("Amoeba Monitor Connection Manager");
		this.appplicationName = appplicationName;
	}

	protected void initServerSocket(){
		Random random = new Random();
        try {
            // create a listening socket and add it to the select set
            ssocket = ServerSocketChannel.open();
            ssocket.configureBlocking(false);
            InetSocketAddress isa = null;
            int times = 0;
    		do{
    		try {
    			if(port <=0){
    				port = random.nextInt(65535);
    			}
    			
	            
	            if (ipAddress != null) {
	                isa = new InetSocketAddress(ipAddress, port);
	            } else {
	                isa = new InetSocketAddress(port);
	            }
	
	            ssocket.socket().bind(isa);
	            break;
    		} catch (IOException e) {
	    			if(times >100){
	    				System.out.println("cannot create shutdownServer socket,System exit now!");
	    				e.printStackTrace();
	    				System.exit(-1);
	    			}
	    		}
    		}while(true);
            
            
            registerServerChannel(ssocket);

            Level level = log.getLevel();
            log.setLevel(Level.INFO);
            log.info("monitor Server listening on " + isa + ".");
            
            log.setLevel(level);
    		} catch (IOException ioe) {
                log.error("Failure listening to socket on port '" + port + "'.", ioe);
                System.err.println("Failure listening to socket on port '" + port + "'.");
                ioe.printStackTrace();
                System.exit(-1);
            }
    		
    		try {
	    		FileWriter writer = new FileWriter(socketInfoFile);
				writer.write(""+port);
				writer.flush();
				writer.close();
    		} catch (IOException e) {
				System.out.println("cannot create shutdownServer socket,System exit now!");
				e.printStackTrace();
				System.exit(-1);
    		}
    }
	
	public static void main(String[] args) throws Exception{
		MonitorServer server = new MonitorServer("amoeba");
		server.init();
		server.run();
	}
}
