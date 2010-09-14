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
package com.meidusa.amoeba.mongodb.io;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public interface MongodbPacketConstant {
	public static final int HEADER_SIZE = 24;
	public static final byte[] HEADER_PAD = new byte[HEADER_SIZE];
	
	public static int OP_REPLY =  1;//  Reply to a client request. responseTo is set  
	public static int OP_MSG = 1000;//  generic msg command followed by a string  
	public static int OP_UPDATE = 2001;//  update document  
	public static int OP_INSERT = 2002;//  insert new document  
	public static int RESERVED = 2003;//  formerly used for OP_GET_BY_OID  
	public static int OP_QUERY = 2004;//  query a collection  
	public static int OP_GET_MORE = 2005;//  Get more data from a query. See Cursors  
	public static int OP_DELETE = 2006;//  Delete documents  
	public static int OP_KILL_CURSORS=  2007;//  Tell database client is done with a cursor
}
