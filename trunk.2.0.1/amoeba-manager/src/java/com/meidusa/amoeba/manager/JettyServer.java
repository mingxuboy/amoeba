package com.meidusa.amoeba.manager;

import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.xml.XmlConfiguration;

import com.meidusa.amoeba.config.ConfigUtil;

/**
 * 
 * @author struct
 *
 */
public class JettyServer {
	protected static Logger logger = Logger.getLogger(JettyServer.class);
    public static void main(String[] args) {
    	
    	String jettyConf = System.getProperty("jetty.conf", "${amoeba.home}/conf/jetty.xml");
    	String rootContext = System.getProperty("rootContext", "${amoeba.home}/htdocs");
    	jettyConf = ConfigUtil.filter(jettyConf);
    	rootContext = ConfigUtil.filter(rootContext);
        Server server = new Server();

        server.setHandler(new DefaultHandler());
        XmlConfiguration configuration = null;
        try {
            configuration = new XmlConfiguration(new FileInputStream(jettyConf));
            configuration.configure(server);
            server.start();
            //server.join();
        } catch (Exception e) {
        	logger.error("Jetty Server fail to start",e);
        	System.exit(-1);
        }
    }
}
