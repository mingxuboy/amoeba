package com.meidusa.amoeba.net.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import com.meidusa.amoeba.net.poolable.PoolableObjectFactory;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;

/**
 * jdbc driver connection factory
 * @author struct
 *
 */
public class JdbcConnectionFactory implements PoolableObjectFactory,Initialisable {
	private String driverName;
	private String url;
	private Driver driver;
	private Properties properties;

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void activateObject(Object obj) throws Exception {
	}

	public void destroyObject(Object obj) throws Exception {
		if(obj instanceof Connection){
			Connection conn = (Connection)obj;
			try {
				if(!conn.isClosed()){
					conn.close();
				}
			} catch (SQLException e) {
			}
		}
	}

	public Object makeObject() throws Exception {
		return new PoolableJdbcConnection(driver.connect(url, properties));
	}

	public void passivateObject(Object obj) throws Exception {

	}

	public boolean validateObject(Object obj) {
		if(obj instanceof Connection){
			Connection conn = (Connection)obj;
			try {
				return !conn.isClosed();
			} catch (SQLException e) {
				return false;
			}
		}
		return false;
	}

	public void init() throws InitialisationException {
		try {
			driver = (Driver)Class.forName(driverName).newInstance();
		} catch (Exception e) {
			throw new InitialisationException(e);
		}
	}

}
