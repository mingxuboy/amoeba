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
package com.meidusa.amoeba.mysql.context;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.util.CharsetMapping;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class MysqlProxyRuntimeContext extends ProxyRuntimeContext {
	public final static String SERVER_VERSION = "5.1.45-mysql-amoeba-proxy-1.4.0-BETA";
    public static final String DEFAULT_SERVER_CONNECTION_FACTORY_CLASS = "com.meidusa.amoeba.mysql.net.MysqlServerConnectionFactory";

    private byte               serverCharsetIndex;

    public MysqlProxyRuntimeContext(){
        ProxyRuntimeContext.setInstance(this);
    }

    public void setServerCharsetIndex(byte serverCharsetIndex) {
        this.serverCharsetIndex = serverCharsetIndex;
        this.setServerCharset(CharsetMapping.INDEX_TO_CHARSET[serverCharsetIndex & 0xff]);
    }

    public byte getServerCharsetIndex() {
        if (serverCharsetIndex > 0) return serverCharsetIndex;
        return CharsetMapping.getCharsetIndex(this.getServerCharset());
    }

    @Override
    protected String getDefaultServerConnectionFactoryClassName() {
        return DEFAULT_SERVER_CONNECTION_FACTORY_CLASS;
    }

}
