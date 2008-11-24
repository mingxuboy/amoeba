/**
 * <pre>
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * @author hexianmao
 */
public class ProxyServerConfig {

    private String                              ipAddress;
    private int                                 port                     = 8066;
    private String                              user;
    private String                              password;
    private int                                 readThreadPoolSize       = 16;
    private int                                 clientSideThreadPoolSize = 16;
    private int                                 serverSideThreadPoolSize = 16;
    private boolean                             tcpNoDelay               = false;
    private int                                 netBufferSize            = 16;
    private String                              serverCharset            = "utf8";

    private Map<String, BeanObjectEntityConfig> managers                 = new HashMap<String, BeanObjectEntityConfig>();
    private Map<String, BeanObjectEntityConfig> unmodifiableManagers     = Collections.unmodifiableMap(managers);

    private Map<String, DBServerConfig>         dbServers                = new HashMap<String, DBServerConfig>();
    private Map<String, DBServerConfig>         unmodifiableDbServers    = Collections.unmodifiableMap(dbServers);

    private BeanObjectEntityConfig              queryRouterConfig;

    public void addManager(String name, BeanObjectEntityConfig managerConfig) {
        managers.put(name, managerConfig);
    }

    public Map<String, BeanObjectEntityConfig> getManagers() {
        return unmodifiableManagers;
    }

    public Map<String, DBServerConfig> getDbServers() {
        return unmodifiableDbServers;
    }

    public void addServer(String name, DBServerConfig serverConfig) {
        dbServers.put(name, serverConfig);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getReadThreadPoolSize() {
        return readThreadPoolSize;
    }

    public void setReadThreadPoolSize(int readThreadPoolSize) {
        this.readThreadPoolSize = readThreadPoolSize;
    }

    public int getServerSideThreadPoolSize() {
        return serverSideThreadPoolSize;
    }

    public void setServerSideThreadPoolSize(int serverSideThreadPoolSize) {
        this.serverSideThreadPoolSize = serverSideThreadPoolSize;
    }

    public int getClientSideThreadPoolSize() {
        return clientSideThreadPoolSize;
    }

    public void setClientSideThreadPoolSize(int clientSideThreadPoolSize) {
        this.clientSideThreadPoolSize = clientSideThreadPoolSize;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BeanObjectEntityConfig getQueryRouterConfig() {
        return queryRouterConfig;
    }

    public void setQueryRouterConfig(BeanObjectEntityConfig queryRouterConfig) {
        this.queryRouterConfig = queryRouterConfig;
    }

    public int getNetBufferSize() {
        return netBufferSize;
    }

    public void setNetBufferSize(int netBufferSize) {
        this.netBufferSize = netBufferSize;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public String getServerCharset() {
        return serverCharset;
    }

    public void setServerCharset(String serverCharset) {
        this.serverCharset = serverCharset;
    }

}
