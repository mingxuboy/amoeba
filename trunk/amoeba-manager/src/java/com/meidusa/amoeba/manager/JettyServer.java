package com.meidusa.amoeba.manager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.xml.XmlConfiguration;
import org.xml.sax.SAXException;

import com.meidusa.amoeba.config.ConfigUtil;

/**
 * 
 * @author struct
 *
 */
public class JettyServer {

    public static void main(String[] args) {
    	String jettyConf = System.getProperty("jetty.conf", "${amoeba.home}/conf/jetty.xml");
    	String rootContext = System.getProperty("jetty.conf", "${amoeba.home}/htdocs");
    	jettyConf = ConfigUtil.filter(jettyConf);
    	rootContext = ConfigUtil.filter(rootContext);
        Server server = new Server();

        server.setHandler(new DefaultHandler());
        XmlConfiguration configuration = null;
        try {
            configuration = new XmlConfiguration(new FileInputStream(jettyConf));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        try {    
            configuration.configure(server);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
