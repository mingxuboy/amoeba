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
package com.meidusa.amoeba.mysql.net;

import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.context.MysqlProxyRuntimeContext;
import com.meidusa.amoeba.mysql.io.MySqlPacketConstant;
import com.meidusa.amoeba.mysql.net.packet.AuthenticationPacket;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.HandshakePacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.util.CharsetMapping;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 设计为连接mysql server的客户端Connection
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class MysqlServerConnection extends MysqlConnection implements MySqlPacketConstant,Reporter.SubReporter,CommandListener,PoolableObject{
	static Logger logger = Logger.getLogger(MysqlServerConnection.class);
	
	/**
	 * 默认与mysql服务器连接采用UTF8，当mysqlServerConnection 编码与 mysqlClientConnection 编码不一致的时候，
	 * 则在query之前会发送set names charset(客户端的相应编码)
	 */
	private static int DEFAULT_CHARSET_INDEX = 33;
	
	public static enum Status{WAITE_HANDSHAKE,AUTHING,COMPLETED};
	private Status status = Status.WAITE_HANDSHAKE;
	private CommandInfo commandInfo = null;
	private CommandMessageQueueRunner commandRunner;
	private ObjectPool objectPool;
	private long createTime = System.currentTimeMillis();
	private boolean active;
	private long serverCapabilities;

	private String serverVersion;

	private int serverMajorVersion;

	private int serverMinorVersion;

	private int serverSubMinorVersion;
	
	public MysqlServerConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
		//commandRunner = new CommandMessageQueueRunner(this);
	}
	
	public void handleMessage(Connection conn,byte[] message) {
		
		if(!isAuthenticated()){
			/**
			 * 第一次数据为 handshake packet
			 * 第二次数据为 OkPacket packet or ErrorPacket 
			 * 
			 */
			MysqlPacketBuffer buffer = new MysqlPacketBuffer(message);
			if(MysqlPacketBuffer.isErrorPacket(message)){
				setAuthenticated(false);
				ErrorPacket error = new ErrorPacket();
				error.init(message,conn);
				logger.error("handShake with "+this._channel.socket().getRemoteSocketAddress()+" error:"+error.serverErrorMessage);
				return;
			}
			
			if(status == Status.WAITE_HANDSHAKE){
				HandshakePacket handpacket = new HandshakePacket();
				handpacket.init(buffer);
				this.serverCapabilities = handpacket.serverCapabilities;
		        this.serverVersion = handpacket.serverVersion;
		        splitVersion();
		        if (!versionMeetsMinimum(4, 1, 1) || handpacket.protocolVersion != 10){
		        	logger.error("amoeba support version minimum 4.1.1  and protocol version 10");
		        	System.exit(-1);
		        }
		        
				if(logger.isDebugEnabled()){
					logger.debug("receive HandshakePacket packet from server:"+this.host +":"+this.port);
				}
				MysqlProxyRuntimeContext context = ((MysqlProxyRuntimeContext)MysqlProxyRuntimeContext.getInstance());
				if(context.getServerCharset() == null && handpacket.serverCharsetIndex > 0){
					context.setServerCharsetIndex(handpacket.serverCharsetIndex);
					logger.info("mysql server Handshake= "+handpacket.toString());
				}
				
				
				AuthenticationPacket authing = new AuthenticationPacket();
				authing.password = this.getPassword();
				authing.seed = handpacket.seed+handpacket.restOfScrambleBuff;
				authing.clientParam = CLIENT_FOUND_ROWS;
				authing.charsetNumber = (byte)(DEFAULT_CHARSET_INDEX & 0xff);
				this.clientCharset = CharsetMapping.INDEX_TO_CHARSET[DEFAULT_CHARSET_INDEX];
				
				if (versionMeetsMinimum(4, 1, 0)) {
		            if (versionMeetsMinimum(4, 1, 1)) {
		            	authing.clientParam |= CLIENT_PROTOCOL_41;
		                // Need this to get server status values
		            	authing.clientParam |= CLIENT_TRANSACTIONS;

		                // We always allow multiple result sets
		            	authing.clientParam |= CLIENT_MULTI_RESULTS;

		                // We allow the user to configure whether
		                // or not they want to support multiple queries
		                // (by default, this is disabled).
		                /*if (this.connection.getAllowMultiQueries()) {
		                    this.clientParam |= CLIENT_MULTI_QUERIES;
		                }*/
		            } else {
		            	authing.clientParam |= CLIENT_RESERVED;
		            }
		        }
				
				if (handpacket.protocolVersion > 9) {
					authing.clientParam |= CLIENT_LONG_PASSWORD; // for long passwords
		        } else {
		        	authing.clientParam &= ~CLIENT_LONG_PASSWORD;
		        }
				
				if ((this.serverCapabilities & CLIENT_LONG_FLAG) != 0) {
					authing.clientParam |= CLIENT_LONG_FLAG;
		        }
				
				if ((this.serverCapabilities & CLIENT_SECURE_CONNECTION) != 0) {
					authing.clientParam |= CLIENT_SECURE_CONNECTION;
				}
				
				authing.user = this.getUser();
				authing.packetId = 1;
				
				if(this.getSchema() != null){
					authing.database = this.getSchema();
					authing.clientParam |= CLIENT_CONNECT_WITH_DB;
				}
				
				authing.maxThreeBytes = 1073741824;
				
				status = Status.AUTHING;
				if(logger.isDebugEnabled()){
					logger.debug("authing packet sent to server:"+this.host +":"+this.port);
				}
				this.postMessage(authing.toByteBuffer(conn).array());
			}else if(status == Status.AUTHING){
				if(logger.isDebugEnabled()){
					logger.debug("authing result packet from server:"+this.host +":"+this.port);
				}
				setAuthenticated(true);
				
				if(MysqlPacketBuffer.isOkPacket(message)){
					return;
				}else{
					logger.warn("server response packet from :"+this._channel.socket().getRemoteSocketAddress()+" :\n"+StringUtil.dumpAsHex(message, message.length));
				}
				
			}

		}else{
			logger.warn("server "+this._channel.socket().getRemoteSocketAddress()+" raw handler message:"+StringUtil.dumpAsHex(message, message.length));
		}
		
	}
	
	public boolean versionMeetsMinimum(int major, int minor, int subminor) {
        if (getServerMajorVersion() >= major) {
            if (getServerMajorVersion() == major) {
                if (getServerMinorVersion() >= minor) {
                    if (getServerMinorVersion() == minor) {
                        return (getServerSubMinorVersion() >= subminor);
                    }

                    // newer than major.minor
                    return true;
                }

                // older than major.minor
                return false;
            }

            // newer than major  
            return true;
        }

        return false;
    }
	
    /**
     * Get the major version of the MySQL server we are talking to.
     *
     * @return DOCUMENT ME!
     */
    final int getServerMajorVersion() {
        return this.serverMajorVersion;
    }

    /**
     * Get the minor version of the MySQL server we are talking to.
     *
     * @return DOCUMENT ME!
     */
    final int getServerMinorVersion() {
        return this.serverMinorVersion;
    }

    /**
     * Get the sub-minor version of the MySQL server we are talking to.
     *
     * @return DOCUMENT ME!
     */
    final int getServerSubMinorVersion() {
        return this.serverSubMinorVersion;
    }

    /**
     * Get the version string of the server we are talking to
     *
     * @return DOCUMENT ME!
     */
    String getServerVersion() {
        return this.serverVersion;
    }
	private void splitVersion(){
		// Parse the server version into major/minor/subminor
        int point = this.serverVersion.indexOf("."); //$NON-NLS-1$

        if (point != -1) {
            try {
                int n = Integer.parseInt(this.serverVersion.substring(0, point));
                this.serverMajorVersion = n;
            } catch (NumberFormatException NFE1) {
                ;
            }

            String remaining = this.serverVersion.substring(point + 1,
                    this.serverVersion.length());
            point = remaining.indexOf("."); //$NON-NLS-1$

            if (point != -1) {
                try {
                    int n = Integer.parseInt(remaining.substring(0, point));
                    this.serverMinorVersion = n;
                } catch (NumberFormatException nfe) {
                    ;
                }

                remaining = remaining.substring(point + 1, remaining.length());

                int pos = 0;

                while (pos < remaining.length()) {
                    if ((remaining.charAt(pos) < '0') ||
                            (remaining.charAt(pos) > '9')) {
                        break;
                    }

                    pos++;
                }

                try {
                    int n = Integer.parseInt(remaining.substring(0, pos));
                    this.serverSubMinorVersion = n;
                } catch (NumberFormatException nfe) {
                    ;
                }
            }
        }
	}
	
	/**
	 * 正在处于验证的Connection Idle时间可以设置相应的少一点。
	 */
	public boolean checkIdle(long now) {
		if(isAuthenticated()){
			if(_handler instanceof Sessionable){
				/**
				 * 该处在高并发的情况下可能会发生ClassCastException 异常,为了提升性能,这儿将忽略这种异常.
				 */
				try{
					Sessionable session = (Sessionable)_handler;
					return session.checkIdle(now);
				}catch(ClassCastException castException){
					return false;
				}
			}
			return false;
		}else{
			long idleMillis = now - _lastEvent;
			if (idleMillis < 15000) {
				return false;
			}
			logger.warn("Disconnecting non-communicative server [conn=" + this
					+ (this.getChannel() != null?","+this.getChannel().socket():", socket closed!") +", idle=" + idleMillis + "ms]. life="+(System.currentTimeMillis()-createTime));
			return true;
		}
	}
	
	/**
	 * commandInfo 存在，并且runner没启动，则需要执行runner。
	 * runner没结束，并且command已经结束，则需要被blocked住
	 * runner running and command not end：runner handle those messages
	 */
	protected void messageProcess(byte[] msg){
		if(commandInfo != null){
			if(commandRunner != null){
				if(commandRunner.getRunnerStatus() != CommandMessageQueueRunner.RunnerStatus.RUNNING){
					final Lock lock = commandRunner.getLock();
					lock.lock();
					try{
						commandRunner.setRunnerStatus(CommandMessageQueueRunner.RunnerStatus.RUNNING);
						ProxyRuntimeContext.getInstance().getServerSideExecutor().execute(commandRunner);
					}finally{
			        	lock.unlock();
			        }
				}
				commandRunner.handleMessage(this, msg);
			}else{
				super.messageProcess(msg);
			}
		}else{
			super.messageProcess(msg);
		}
	}

	
	public void appendReport(StringBuilder buffer, long now, long sinceLast,
			boolean reset,Level level) {
		
		if(commandRunner != null){
			buffer.append("    -- Command: messageQueueSize:").append(commandRunner.getQueueSize());
			buffer.append(",runner.Status:").append(commandRunner.getRunnerStatus()).append("\n");
		}
		
		if(this._handler instanceof Reporter.SubReporter && this._handler != this){
			Reporter.SubReporter reporter = (Reporter.SubReporter)(this._handler);
			reporter.appendReport(buffer, now, sinceLast, reset,level);
		}
	}

	
	public void finishedCommand(CommandInfo command) {
		if(commandRunner != null){
			final Lock lock = commandRunner.getLock();
		    lock.lock();
			try{
		    	commandRunner.setRunnerStatus(CommandMessageQueueRunner.RunnerStatus.WAITTOEND);
				this.commandInfo = null;
		    }finally{
		    	lock.unlock();
		    }
		}
	}

	public void startCommand(CommandInfo command) {
	    this.commandInfo = command;
	}

	public ObjectPool getObjectPool() {
		return objectPool;
	}

	public synchronized void setObjectPool(ObjectPool pool) {
		if(objectPool == null && pool == null){
			if(this.isAuthenticated()){
				logger.warn("Set pool null",new Exception());
			}
		}
		this.objectPool = pool;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isRemovedFromPool() {
		return objectPool == null;
	}
	
	protected void close(Exception exception){
		super.close(exception);
		final ObjectPool tmpPool = objectPool;
		objectPool = null;
		try {
			if(tmpPool != null){
				
				/**
				 * 处于active 状态的 poolableObject，可以用ObjectPool.invalidateObject 方式从pool中销毁
				 * 否则只能等待被borrow 或者 idle time out
				 */
				if(isActive()){
					tmpPool.invalidateObject(this);
				}
			}
		} catch (Exception e) {
		}
		
		if(commandRunner != null && commandRunner.getRunnerStatus() == CommandMessageQueueRunner.RunnerStatus.RUNNING){
			commandRunner.interrupt();
		}
	}
	
}
