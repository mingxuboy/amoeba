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
package com.meidusa.amoeba.mysql.handler;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.packet.EOFPacket;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.OKforPreparedStatementPacket;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.net.packet.PacketBuffer;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class PreparedStatmentInfo {

    /**
     * 客户端发送过来的 prepared statment sql语句
     */
    private String preparedStatment;

    private int    parameterCount;

    /**
     * 需要返回给客户端
     */
    private byte[] packetBuffer;

    private long   statmentId;

    public PreparedStatmentInfo(DatabaseConnection conn, long id, String preparedSql){
        PacketBuffer buffer = new AbstractPacketBuffer(2048);
        statmentId = id;
        this.preparedStatment = preparedSql;
        OKforPreparedStatementPacket okPaket = new OKforPreparedStatementPacket();
        okPaket.columns = 1;
        okPaket.packetId = 1;
        byte packetId = 1;
        parameterCount = ProxyRuntimeContext.getInstance().getQueryRouter().parseParameterCount(conn, preparedSql);
        okPaket.parameters = parameterCount;
        okPaket.statementHandlerId = statmentId;
        buffer.writeBytes(okPaket.toByteBuffer(conn).array());
        if (parameterCount > 0) {
            for (int i = 0; i < parameterCount; i++) {
                FieldPacket field = new FieldPacket();
                field.packetId = (byte) (++packetId);

                buffer.writeBytes(field.toByteBuffer(conn).array());
            }
            EOFPacket eof = new EOFPacket();
            eof.packetId = ++packetId;
            eof.serverStatus = 2;

            buffer.writeBytes(eof.toByteBuffer(conn).array());
        }

        if (okPaket.columns > 0) {
            for (int i = 0; i < okPaket.columns; i++) {
                FieldPacket field = new FieldPacket();
                field.packetId = (byte) (++packetId);
                field.length = 8;
                field.type = (byte) MysqlDefs.FIELD_TYPE_VAR_STRING;
                buffer.writeBytes(field.toByteBuffer(conn).array());
            }
            EOFPacket eof = new EOFPacket();
            eof.packetId = ++packetId;
            eof.serverStatus = 2;
            buffer.writeBytes(eof.toByteBuffer(conn).array());
        }
        packetBuffer = buffer.toByteBuffer().array();
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public byte[] getByteBuffer() {
        return packetBuffer;
    }

    public long getStatmentId() {
        return statmentId;
    }

    public String getPreparedStatment() {
        return preparedStatment;
    }

    public void setPreparedStatment(String preparedStatment) {
        this.preparedStatment = preparedStatment;
    }
}
