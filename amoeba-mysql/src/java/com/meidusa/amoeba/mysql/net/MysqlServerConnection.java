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
import java.security.NoSuchAlgorithmException;
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
import com.meidusa.amoeba.mysql.util.Security;
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
				MysqlProxyRuntimeContext context = ((MysqlProxyRuntimeContext)MysqlProxyRuntimeContext.getInstance());
				if(context.getServerCharset() == null && handpacket.serverCharsetIndex > 0){
					context.setServerCharsetIndex(handpacket.serverCharsetIndex);
				}
				AuthenticationPacket authing = new AuthenticationPacket();
				authing.charsetNumber = (byte)(DEFAULT_CHARSET_INDEX & 0xff);
				this.clientCharset = CharsetMapping.INDEX_TO_CHARSET[DEFAULT_CHARSET_INDEX];
				authing.clientParam = CLIENT_LONG_PASSWORD|CLIENT_PROTOCOL_41|CLIENT_LONG_FLAG
									|CLIENT_FOUND_ROWS|CLIENT_TRANSACTIONS|CLIENT_SECURE_CONNECTION|CLIENT_MULTI_RESULTS;
				authing.user = this.getUser();
				authing.packetId = 1;
				
				if(this.getSchema() != null){
					authing.database = this.getSchema();
					authing.clientParam |= CLIENT_CONNECT_WITH_DB;
				}
				
				authing.maxThreeBytes = 1073741824;
				if(this.getPassword() != null){
					try {
						authing.encryptedPassword = Security.scramble411(this.getPassword(),handpacket.seed+handpacket.restOfScrambleBuff);
					} catch (NoSuchAlgorithmException e) {
						logger.error("encrypt Password error",e);
					}
				}
				status = Status.AUTHING;
				this.postMessage(authing.toByteBuffer(conn).array());
			}else if(status == Status.AUTHING){
				if(MysqlPacketBuffer.isOkPacket(message)){
					setAuthenticated(true);
					return;
				}
			}
		}else{
			logger.warn("server "+this._channel.socket().getRemoteSocketAddress()+" raw handler message:"+StringUtil.dumpAsHex(message, message.length));
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
