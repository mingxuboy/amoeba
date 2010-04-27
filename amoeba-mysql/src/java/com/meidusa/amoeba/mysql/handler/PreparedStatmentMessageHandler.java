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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.meidusa.amoeba.mysql.net.CommandInfo;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.MysqlServerConnection;
import com.meidusa.amoeba.mysql.net.packet.CommandPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.OKforPreparedStatementPacket;
import com.meidusa.amoeba.mysql.net.packet.PreparedStatmentClosePacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.statment.Statment;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class PreparedStatmentMessageHandler extends QueryCommandMessageHandler {
	
	private List<byte[]> preparedStatmentBytes = new ArrayList<byte[]>();
	private Map<Connection,List<byte[]>> preparedMap = new HashMap<Connection,List<byte[]>>();
    static class PreparedStatmentSessionStatus extends SessionStatus {

        public static final int PREPAED_PARAMETER_EOF = 2048;
        public static final int PREPAED_FIELD_EOF     = 4096;
    }

    static class PreparedStatmentConnectionStatuts extends QueryCommandMessageHandler.QueryConnectionStatus {

        OKforPreparedStatementPacket ok = null;

        public PreparedStatmentConnectionStatuts(Connection conn, PreparedStatmentInfo preparedStatmentInfo){
            super(conn);
        }

        /**
         * packet step: 1:OKforPreparedStatementPacket, (parameters ==0,columns==0) end; parameters>0 [
         * n*parameterFieldPacket,PREPAED_PARAMETER_EOF] columns>0 [n * columnPacket,PREPAED_FIELD_EOF] mysql version
         * 5.0.0 no parameter field packet
         */
        @Override
        public boolean isCompleted(byte[] buffer) {
            if (this.commandType == QueryCommandPacket.COM_STMT_PREPARE) {
                MysqlServerConnection connection = (MysqlServerConnection) conn;
                if (MysqlPacketBuffer.isEofPacket(buffer)) {
                    if (ok.parameters > 0 && ok.columns > 0) {
                        if ((this.statusCode & PreparedStatmentSessionStatus.PREPAED_PARAMETER_EOF) > 0) {
                            this.statusCode |= PreparedStatmentSessionStatus.PREPAED_FIELD_EOF;
                            this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
                            return true;
                        } else {
                            if (connection.isVersion(5, 0, 0)) {
                                if (ok.columns == 0) {
                                    this.statusCode |= PreparedStatmentSessionStatus.PREPAED_FIELD_EOF;
                                    this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
                                    return true;
                                }
                            }
                            this.statusCode |= PreparedStatmentSessionStatus.PREPAED_PARAMETER_EOF;
                            return false;
                        }
                    } else {
                        this.statusCode |= PreparedStatmentSessionStatus.PREPAED_FIELD_EOF;
                        this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
                        return true;
                    }

                } else if (MysqlPacketBuffer.isErrorPacket(buffer)) {
                    this.statusCode |= PreparedStatmentSessionStatus.ERROR;
                    this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
                    return true;
                } else if (packetIndex == 0 && MysqlPacketBuffer.isOkPacket(buffer)) {
                    ok = new OKforPreparedStatementPacket();
                    ok.init(buffer, null);
                    if (ok.parameters == 0 && ok.columns == 0) {
                        this.statusCode |= PreparedStatmentSessionStatus.OK;
                        this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
                        return true;
                    } else {
                        if (connection.isVersion(5, 0, 0)) {
                            if (ok.columns == 0) {
                                this.statusCode |= PreparedStatmentSessionStatus.OK;
                                this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
                                return true;
                            }
                        }
                        return false;
                    }
                }
                return false;
            } else if (this.commandType == QueryCommandPacket.COM_STMT_CLOSE) {
                if (MysqlPacketBuffer.isErrorPacket(buffer)) {
                    this.statusCode |= PreparedStatmentSessionStatus.ERROR;
                    this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
                    return true;
                } else {
                    this.statusCode |= PreparedStatmentSessionStatus.COMPLETED;
                    return true;
                }
            } else {
                return super.isCompleted(buffer);
            }
        }
    }

    protected PreparedStatmentInfo preparedStatmentInfo = null;
    /** 当前的请求数据包 */
    private Map<Connection, Long>  statmentIdMap        = Collections.synchronizedMap(new HashMap<Connection, Long>());
	private boolean isExecute;

    public PreparedStatmentMessageHandler(MysqlClientConnection source, PreparedStatmentInfo preparedStatmentInfo,Statment statment,
                                          byte[] query, ObjectPool[] pools, long timeout,boolean isExecute){
        super(source, query,statment, pools, timeout);
        this.preparedStatmentInfo = preparedStatmentInfo;
        this.isExecute = isExecute;
    }

    protected void afterCommandCompleted(CommandInfo currentCommand) {
        if (commandType == QueryCommandPacket.COM_STMT_PREPARE) {
            Collection<ConnectionStatuts> collection = this.commandQueue.connStatusMap.values();
            for (ConnectionStatuts status : collection) {
                byte[] buffer = status.buffers.get(0);
                OKforPreparedStatementPacket ok = new OKforPreparedStatementPacket();
                ok.init(buffer, source);
                statmentIdMap.put(status.conn, ok.statementHandlerId);
            }
        }
        super.afterCommandCompleted(currentCommand);
    }

    @Override
    protected List<byte[]> mergeMessages() {
        if (commandType == QueryCommandPacket.COM_STMT_PREPARE) {
            List<byte[]> list = new ArrayList<byte[]>(16);
            Collection<ConnectionStatuts> statusList = this.commandQueue.connStatusMap.values();
            ConnectionStatuts status = statusList.iterator().next();
            list.addAll(status.buffers);
            return list;
        } else {
            return super.mergeMessages();
        }
    }

    protected void dispatchMessageFrom(Connection fromConn,byte[] message){
    	if(fromConn != source){
    		if (commandType == QueryCommandPacket.COM_STMT_PREPARE) {
    			if(!isExecute){
    				if(MysqlPacketBuffer.isOkPacket(message)){
            			OKforPreparedStatementPacket ok = new OKforPreparedStatementPacket(); 
            			ok.init(message,fromConn);
            			ok.statementHandlerId = preparedStatmentInfo.getStatmentId();
            			message = ok.toByteBuffer(fromConn).array();
            		}
    				preparedStatmentInfo.addPacket(message);
    			}
    		}
    	}
    	super.dispatchMessageFrom(fromConn, message);
    }
    /**
     * 替换相应的 prepared Statment id，保存相应的数据包,并且填充 preparedStatmentInfo 的一些信息
     */
    protected void dispatchMessageTo(Connection toConn, byte[] message) {
        if (toConn == source) {
            if (commandType == QueryCommandPacket.COM_STMT_PREPARE) {
                /*
                 * if(MysqlPacketBuffer.isOkPacket(message)){ //替换statmentId 为 proxy statment id 发送到mysql客户端
                 * OKforPreparedStatementPacket ok = new OKforPreparedStatementPacket(); ok.init(message,toConn);
                 * ok.statementHandlerId = preparedStatmentInfo.getStatmentId(); preparedStatmentInfo.setOkPrepared(ok);
                 * message = ok.toByteBuffer(toConn).array(); }
                 */
            	//source.get
            	/*if(!isExecute){
            		preparedStatmentBytes.add(message);
            	}*/
            	if(isExecute){
            		return;
            	}else{
            		if(MysqlPacketBuffer.isOkPacket(message)){
            			OKforPreparedStatementPacket ok = new OKforPreparedStatementPacket(); 
            			ok.init(message,toConn);
            			ok.statementHandlerId = preparedStatmentInfo.getStatmentId();
            			message = ok.toByteBuffer(toConn).array();
            		}
            	}
            }
        } else {
            if (commandType == CommandPacket.COM_STMT_EXECUTE || commandType == CommandPacket.COM_STMT_SEND_LONG_DATA || commandType == CommandPacket.COM_STMT_CLOSE) {
                Long id = statmentIdMap.get(toConn);
                message[5] = (byte) (id & 0xff);
                message[6] = (byte) (id >>> 8);
                message[7] = (byte) (id >>> 16);
                message[8] = (byte) (id >>> 24);
            }
        }
        super.dispatchMessageTo(toConn, message);
    }

    protected void appendAfterMainCommand() {
        super.appendAfterMainCommand();
        PreparedStatmentClosePacket preparedCloseCommandPacket = new PreparedStatmentClosePacket();
        preparedCloseCommandPacket.command = CommandPacket.COM_STMT_CLOSE;
        final byte[] buffer = preparedCloseCommandPacket.toByteBuffer(source).array();
        CommandInfo info = new CommandInfo();
        info.setBuffer(buffer);
        info.setMain(false);
        info.getCompletedCount().set(commandQueue.connStatusMap.size());
        info.setRunnable(new Runnable() {

            public void run() {
                Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
                for (Connection conn : connSet) {
                    statmentIdMap.remove(conn);
                }
            }
        });
        commandQueue.appendCommand(info, true);
    }

    @Override
    protected ConnectionStatuts newConnectionStatuts(Connection conn) {
        return new PreparedStatmentConnectionStatuts(conn, this.preparedStatmentInfo);
    }

    public List<byte[]> getPreparedStatmentBytes(){
    	return this.preparedStatmentBytes;
    }
}
