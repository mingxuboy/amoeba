package com.meidusa.amoeba.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.helpers.LogLog;

import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.net.AuthResponseData;
import com.meidusa.amoeba.net.AuthingableConnection;
import com.meidusa.amoeba.util.IPRule;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.StringUtil;

/**
 * IP ∑√Œ øÿ÷∆π˝¬À IP v4
 * 
 * @author struct
 * @author hexianmao
 */
public class IPAccessController implements AuthenticateFilter, Initialisable {

    protected static Logger     logger        = Logger.getLogger(IPAccessController.class);
    private static final String DENAY_MESSAGE = "Access denied for ip: '${host}' to amoeba server";
    private boolean             isEnabled;
    private String[]            ipRule        = null;
    private String              ipFile;

    public void setIpFile(String ipFile) {
        this.ipFile = ipFile;
    }

    public IPAccessController(){
    }

    private List<String> loadIPRule(File ipFile) {
        List<String> list = new ArrayList<String>();

        if (!ipFile.exists() && !ipFile.isFile()) {
            isEnabled = false;
            return list;
        }

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(ipFile));
            String ipRuleLine = null;

            while ((ipRuleLine = reader.readLine()) != null) {
                ipRuleLine = ipRuleLine.trim();
                if (!StringUtil.isEmpty(ipRuleLine) && !ipRuleLine.startsWith("#")) {
                    try {
                        IPRule.isAllowIP(new String[] { ipRuleLine }, "127.0.0.1");
                        list.add(ipRuleLine);
                    } catch (Exception e) {
                        logger.warn("'" + ipRuleLine + "' error:" + e.getMessage() + "  ,this rule disabled");
                    }
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("ip access control loaded from file:" + ipFile.getAbsolutePath());
            }
            isEnabled = true;
        } catch (FileNotFoundException e) {
            logger.warn(" file:" + ipFile + " not found ,ip access control disabled.");
            isEnabled = false;
        } catch (IOException e) {
            logger.warn(" reading file:" + ipFile + " error ,ip access control disabled.");
            isEnabled = false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return list;
    }

    public boolean doFilte(AuthingableConnection conn, AuthResponseData rdata) {
        if (isEnabled) {
            if (ipRule != null && ipRule.length > 0) {
                String ip = conn.getInetAddress().getHostAddress();
                try {
                    boolean access = IPRule.isAllowIP(ipRule, ip);
                    if (!access) {
                        Properties properties = new Properties();
                        properties.setProperty("host", ip);
                        rdata.message = ConfigUtil.filter(DENAY_MESSAGE, properties);
                    }
                    return access;
                } catch (Exception e) {
                    logger.warn(ip + " check access error:", e);
                }
            }
        }

        return true;
    }

    private class IPAccessFileWatchdog extends FileWatchdog {

        public IPAccessFileWatchdog(String filename){
            super(filename);
        }

        public void doOnChange() {
            List<String> list = IPAccessController.this.loadIPRule(new File(this.filename));
            if (list != null) {
                IPAccessController.this.ipRule = list.toArray(new String[list.size()]);
            } else {
                IPAccessController.this.ipRule = null;
            }
            LogLog.warn("ip access config load completed from file:" + filename);
        }
    }

    public void init() throws InitialisationException {
        File file = new File(ipFile);
        if (!file.exists() && !file.isFile()) {
            isEnabled = false;
            if (logger.isInfoEnabled()) {
                logger.info("ip access control file not found:" + ipFile + ", ip access controller disabled.");
            }
        }

        IPAccessFileWatchdog dog = new IPAccessFileWatchdog(ipFile);
        dog.setDaemon(true);
        dog.setDelay(FileWatchdog.DEFAULT_DELAY);
        dog.start();
    }

}
