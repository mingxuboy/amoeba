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
package com.meidusa.amoeba.server;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.AuthResponseData;
import com.meidusa.amoeba.net.Authenticator;
import com.meidusa.amoeba.net.AuthingableConnection;

/**
 * 一个相当简单的身份验证者.
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class DummyAuthenticator extends Authenticator {
	protected static Logger logger = Logger.getLogger(DummyAuthenticator.class);
	
	public DummyAuthenticator() {
		
	}
	
	// from abstract Authenticator
	protected void processAuthentication(AuthingableConnection conn,
			AuthResponseData rdata) {
		logger.info("Accepting request: conn=" + conn);
		rdata.code = AuthResponseData.SUCCESS;
	}

}
