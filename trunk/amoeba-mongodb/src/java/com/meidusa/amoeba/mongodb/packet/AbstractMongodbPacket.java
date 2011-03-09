/**
 * <pre>
 * Copyright meidusa.com
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacket;


/**
 * <pre>
 * struct MsgHeader {
 *     int32   messageLength; // total message size, including this
 *     int32   requestID;     // identifier for this message
 *     int32   responseTo;    // requestID from the original request
 *                            //   (used in reponses from db)
 *     int32   opCode;        // request type - see table below
 * }
 * 
 * 
 * <table class='confluenceTable'><tbody>
 * <tr>
 * <th class='confluenceTh'> Opcode Name </th>
 * <th class='confluenceTh'> opCode value </th>
 * 
 * <th class='confluenceTh'> Comment </th>
 * </tr>
 * <tr>
 * <td class='confluenceTd'> OP_REPLY </td>
 * <td class='confluenceTd'> 1 </td>
 * <td class='confluenceTd'> Reply to a client request. responseTo is set </td>
 * </tr>
 * <tr>
 * <td class='confluenceTd'> OP_MSG </td>
 * 
 * <td class='confluenceTd'> 1000 </td>
 * <td class='confluenceTd'> generic msg command followed by a string </td>
 * </tr>
 * <tr>
 * <td class='confluenceTd'> OP_UPDATE </td>
 * <td class='confluenceTd'> 2001 </td>
 * <td class='confluenceTd'> update document </td>
 * 
 * </tr>
 * <tr>
 * <td class='confluenceTd'> OP_INSERT </td>
 * <td class='confluenceTd'> 2002 </td>
 * <td class='confluenceTd'> insert new document </td>
 * </tr>
 * <tr>
 * <td class='confluenceTd'> RESERVED </td>
 * <td class='confluenceTd'> 2003 </td>
 * 
 * <td class='confluenceTd'> formerly used for OP_GET_BY_OID </td>
 * </tr>
 * <tr>
 * <td class='confluenceTd'> OP_QUERY </td>
 * <td class='confluenceTd'> 2004 </td>
 * <td class='confluenceTd'> query a collection </td>
 * </tr>
 * <tr>
 * <td class='confluenceTd'> OP_GET_MORE </td>
 * 
 * <td class='confluenceTd'> 2005 </td>
 * <td class='confluenceTd'> Get more data from a query.  See Cursors </td>
 * </tr>
 * <tr>
 * <td class='confluenceTd'> OP_DELETE </td>
 * <td class='confluenceTd'> 2006 </td>
 * <td class='confluenceTd'> Delete documents </td>
 * 
 * </tr>
 * <tr>
 * <td class='confluenceTd'> OP_KILL_CURSORS </td>
 * <td class='confluenceTd'> 2007 </td>
 * <td class='confluenceTd'> Tell database client is done with a cursor </td>
 * </tr>
 * </tbody></table>
 * @see <a href="http://www.mongodb.org/display/DOCS/Mongo+Wire+Protocol#MongoWireProtocol-TableOfContents">mongodb wire protocol</a>
 * </pre>
 * @author Struct
 *
 */
public class AbstractMongodbPacket extends AbstractPacket<MongodbPacketBuffer>{
    public int messageLength; // total message size, including this
    public int requestID;// identifier for this message
    public int responseTo;// requestID from the original request
    public int opCode;// request type - see table below
    
	@Override
	protected void afterPacketWritten(MongodbPacketBuffer buffer) {
		int position = buffer.getPosition();
		buffer.writeInt(0, position);
	}

	@Override
	protected int calculatePacketSize() {
		return 16;
	}

	@Override
	protected Class<MongodbPacketBuffer> getPacketBufferClass() {
		return MongodbPacketBuffer.class;
	}

	@Override
	protected void init(MongodbPacketBuffer buffer) {
		messageLength = buffer.readInt();
		requestID = buffer.readInt();
		responseTo = buffer.readInt();
		opCode = buffer.readInt();
	}

	@Override
	protected void write2Buffer(MongodbPacketBuffer buffer)
			throws UnsupportedEncodingException {
		buffer.writeInt(messageLength);
		buffer.writeInt(requestID);
		buffer.writeInt(responseTo);
		buffer.writeInt(opCode);
	}

}
