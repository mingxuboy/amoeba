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
package com.meidusa.amoeba.mysql.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.meidusa.amoeba.mysql.packet.OKforPreparedStatementPacket;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class PreparedStatmentInfo{
	private final Condition full;
	private final ReentrantLock lock;
	/**
	 * 客户端发送过来的 prepared statment sql语句
	 */
	private String preparedStatment;
	private OKforPreparedStatementPacket okPrepared;
	/**
	 * 需要返回给客户端
	 */
	private List<byte[]> preparedStatmentPackets = new ArrayList<byte[]>();
	
	private long statmentId;
	
	public PreparedStatmentInfo(long id){
		statmentId = id;
		lock = new ReentrantLock(false);
		full = lock.newCondition();
	}
	
	public OKforPreparedStatementPacket getOkPrepared() {
		return okPrepared;
	}

	public void setOkPrepared(OKforPreparedStatementPacket okPrepared) {
		this.okPrepared = okPrepared;
	}
	
	public boolean isReady(){
		return okPrepared != null;
	}
	/**
	 * 
	 * @param buffer
	 */
	public void putPreparedStatmentBuffer(byte[] buffer){
		final ReentrantLock lock = this.lock;
	    lock.lock();
	    try {
			preparedStatmentPackets.add(buffer);
			if(buffersIsFull()){
				full.signalAll();
			}
		} finally {
            lock.unlock();
        }
	}
	
	/**
	 * 替换原来得preparedStatment
	 * @param list
	 */
	protected synchronized void setPreparedBufferList(List<byte[]> list){
		byte[] buffer = list.get(0);
		buffer[5] = (byte) (statmentId & 0xff);
		buffer[6] = (byte) (statmentId >>> 8);
		buffer[7] = (byte) (statmentId >>> 16);
		buffer[8] = (byte) (statmentId >>> 24);
		
		synchronized (this.okPrepared) {
			OKforPreparedStatementPacket ok = new OKforPreparedStatementPacket();
			ok.init(buffer);
			this.okPrepared = ok;
			preparedStatmentPackets = list;
		}
	}
	
	/**
	 * 判断当前的prepared Statment数据包是否完整
	 * preparedStatment数据包数量 = 1个OK数据包 + columns>0?columns+1个EOF:0 + parameters>0?parameters+1个EOF:0
	 * @return
	 */
	boolean buffersIsFull(){
		if(okPrepared == null){
			return false;
		}
		return preparedStatmentPackets.size() ==  (okPrepared.columns>0 ? okPrepared.columns +1:0)
												+ (okPrepared.parameters >0? okPrepared.parameters +1:0)
												+ 1;
	}
	
	/**
	 * 获得prepared statment 服务端返回的数据包。如果数据包还未完整则将会被blocked
	 * @see {@link #buffersIsFull}
	 * @return
	 */
	public List<byte[]> getPreparedStatmentBuffers(){
		if(!buffersIsFull()){
			final ReentrantLock lock = this.lock;
	        try {
	        	lock.lockInterruptibly();
	        	try {
	                while (!buffersIsFull())
	                    full.await();
	            } catch (InterruptedException ie) {
	            	ie.printStackTrace();
	            	full.signalAll(); // propagate to non-interrupted thread
	            }
	        } catch (InterruptedException e) {
	        	e.printStackTrace();
	        	
			}finally{
	        	lock.unlock();
	        }
		}
		return preparedStatmentPackets;
	}

	public long getStatmentId() {
		return statmentId;
	}
	
	
	/**
	 * only for test
	 */
	public static void main(String[] args){
		AtomicLong atomicLong = new AtomicLong(1);
		final PreparedStatmentInfo info = new PreparedStatmentInfo(atomicLong.getAndIncrement());
		new Thread(){
			public void run(){
				while(true){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					if(info.okPrepared == null){
						info.okPrepared = new OKforPreparedStatementPacket();
						info.okPrepared.columns = 3;
						info.okPrepared.parameters = 4;
					}
					
					System.out.println("add buffer....");
					info.putPreparedStatmentBuffer(new byte[]{});
					if(info.buffersIsFull()){
						break;
					}
				}
				
			}
		}.start();
		
		new Thread(){
			public void run(){
				try {
					Thread.sleep(2000l);
				} catch (InterruptedException e) {
				}
				info.getPreparedStatmentBuffers();
				System.out.println("prepared1....");
				
			}
		}.start();
		
		new Thread(){
			public void run(){
				info.getPreparedStatmentBuffers();
				System.out.println("prepared2....");
				
			}
		}.start();
		
		new Thread(){
			public void run(){
				info.getPreparedStatmentBuffers();
				System.out.println("prepared3....");
				
			}
		}.start();
	}

	public String getPreparedStatment() {
		return preparedStatment;
	}

	public void setPreparedStatment(String preparedStatment) {
		this.preparedStatment = preparedStatment;
	}
}