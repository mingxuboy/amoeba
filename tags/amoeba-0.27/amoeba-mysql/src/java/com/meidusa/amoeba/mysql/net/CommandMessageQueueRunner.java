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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.util.IllegalRequestParameterException;
import com.meidusa.amoeba.util.NameableRunner;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class CommandMessageQueueRunner implements NameableRunner,MessageHandler {
	private static Logger logger = Logger.getLogger(CommandMessageQueueRunner.class); 
	static enum RunnerStatus{
		FREE,RUNNING,WAITTOEND,END
	}
	private CommandMessageQueueRunner.RunnerStatus runnerStatus = RunnerStatus.FREE;
	private BlockingQueue<byte[]> messagesQueue = new LinkedBlockingQueue<byte[]>();
	private Connection conn;
	private final Lock lock = new ReentrantLock(false);
	private String name;
	
	private Thread currentThread;
	public CommandMessageQueueRunner(Connection conn){
		this.conn = conn;
		name = "Proccess-"+conn.toString();
	}
	
	public String getRunnerName() {
		return name;
	}
	
	public void run() {
	    lock.lock();
		byte[] msg;
		try {
			currentThread = Thread.currentThread();
			while(runnerStatus == RunnerStatus.RUNNING && (msg = messagesQueue.take()) != null){
				conn.getMessageHandler().handleMessage(conn, msg);
				if(runnerStatus != RunnerStatus.RUNNING){
					if(messagesQueue.size() >0){
						logger.error("messageQueue size:"+messagesQueue.size()+" , but command completed!!conn="+conn);
						messagesQueue.clear();
					}
					break;
				}
			}
		} catch (InterruptedException e) {
		}finally{
			runnerStatus = RunnerStatus.END;
			currentThread = null;
			lock.unlock();
		}
	}
	
	public void interrupt(){
		if(currentThread != null){
			currentThread.interrupt();
		}
	}
	
	public Lock getLock(){
		return lock;
	}

	public void setRunnerStatus(RunnerStatus status){
		this.runnerStatus = status;
	}
	public int getQueueSize(){
		return messagesQueue.size();
	}
	public void handleMessage(Connection conn,byte[] msg){
		if(this.conn != conn){
			throw new IllegalRequestParameterException("Illegal parameter connection ="+conn);
		}
		messagesQueue.offer(msg);
	}

	public CommandMessageQueueRunner.RunnerStatus getRunnerStatus() {
		return runnerStatus;
	}

	
}