/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.config;

public class DBServerConfig extends ConfigEntity implements Cloneable{
	private static final long serialVersionUID = 1L;
	private String name;
	private boolean isVirtual;
	private String parent;
	private BeanObjectEntityConfig factoryConfig;
	private BeanObjectEntityConfig poolConfig;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}
	
	public boolean isVirtual() {
		return isVirtual;
	}

	public void setVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}

	public BeanObjectEntityConfig getFactoryConfig() {
		return factoryConfig;
	}

	public void setFactoryConfig(BeanObjectEntityConfig factoryConfig) {
		this.factoryConfig = factoryConfig;
	}

	public BeanObjectEntityConfig getPoolConfig() {
		return poolConfig;
	}

	public void setPoolConfig(BeanObjectEntityConfig poolConfig) {
		this.poolConfig = poolConfig;
	}
	
	public Object clone(){
		DBServerConfig config = new DBServerConfig();
		config.isVirtual = isVirtual;
		config.name = name;
		config.parent = parent;
		if(factoryConfig != null){
			config.factoryConfig = (BeanObjectEntityConfig)factoryConfig.clone();
		}
		
		if(poolConfig != null){
			config.poolConfig = (BeanObjectEntityConfig)poolConfig.clone();
		}
		
		return config;
	}

}
