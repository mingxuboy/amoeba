/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.meidusa.amoeba.oracle.context;

import com.meidusa.amoeba.context.ProxyRuntimeContext;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class OracleProxyRuntimeContext extends ProxyRuntimeContext {

    public static final String DEFAULT_SERVER_CONNECTION_FACTORY_CLASS = "com.meidusa.amoeba.oracle.net.OracleConnectionFactory";

    public OracleProxyRuntimeContext(){
        ProxyRuntimeContext.setInstance(this);
    }

    @Override
    protected String getDefaultServerConnectionFactoryClassName() {
        return DEFAULT_SERVER_CONNECTION_FACTORY_CLASS;
    }

    /**
     * 这儿暂时不初始化所有连接池
     */
    /*protected void initPools() {
    	
    }*/

}
