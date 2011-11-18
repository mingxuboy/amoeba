/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.gateway.packet;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public interface GatewayPacketConstant {
	public static final int HEADER_SIZE = 28;
	public static final byte[] HEADER_PAD = new byte[HEADER_SIZE];
	public static final int PACKET_TYPE_PING = 0x01000001;
	public static final int PACKET_TYPE_PONG = 0x01000002;
	public static final int PACKET_TYPE_SERVICE_REQUEST = 0x02000001;
	public static final int PACKET_TYPE_SERVICE_RESPONSE = 0x02000002;
	public static final short VERSION_1 = 0x0001;
	public static final short VERSION_2 = 0x0002;
	public static final String PACKET_CHARSET = "UTF8";
	public static final boolean PACKET_CONTENT_ALL_IN_ONE = false;
	public static final String PACKET_CONTENT_SPLITER = "&";
	public static final int TYPE_POSITION = 8;
	public static final short CONTENT_TYPE_JSON = 0x0000;
	public static final short CONTENT_TYPE_XML = 0x0001;
	public static final short CONTENT_TYPE_OBJECT = 0x0002;
}
