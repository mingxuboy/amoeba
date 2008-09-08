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
package com.meidusa.amoeba.net.poolable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 该Pool 提供负载均衡、failover、HA策略
 * 采用Load Balance ObjectPool，则object 必须实现{@link PoolableObject}
 * 默认提供2种负载均衡方案：
 * <li>轮询：请求将轮询分配到每个pool，每个pool的请求比较平均</li>
 * <li>繁忙程度：将所有Pool的Active Num做一个排序，最小的Active Num将优先分配请求
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class MultipleLoadBalanceObjectPool implements ObjectPool{
	
	public static final int LOADBALANCING_ROUNDROBIN = 1;
	public static final int LOADBALANCING_WEIGHTBASED = 2;
	private boolean enable;
	
	protected static class ActiveNumComparator implements Comparator<ObjectPool>{
		public int compare(ObjectPool o1, ObjectPool o2) {
			return o1.getNumActive() - o2.getNumActive();
		}
	}
	
	protected static class HeartbeatDelayed implements Delayed{
		private long time;
		/** Sequence number to break ties FIFO */
        private final long sequenceNumber;
        private long NANO_ORIGIN = System.nanoTime();
        private static final AtomicLong sequencer = new AtomicLong(0);
        private ObjectPool pool;
        private MultipleLoadBalanceObjectPool virtualPool;
		
		public HeartbeatDelayed(long nsTime,TimeUnit timeUnit,ObjectPool pool,MultipleLoadBalanceObjectPool virtualPool){
			this.time = TimeUnit.NANOSECONDS.convert(nsTime, timeUnit);
			this.pool = pool;
			this.virtualPool = virtualPool;
			this.sequenceNumber = sequencer.getAndIncrement();
		}
		
		public void setDelayedTime(long time,TimeUnit timeUnit){
			NANO_ORIGIN = System.nanoTime();
			this.time = TimeUnit.NANOSECONDS.convert(time, timeUnit);
		}
		
		public long getDelay(TimeUnit unit) {
			long d = unit.convert(time - now(), TimeUnit.NANOSECONDS);
            return d;
		}

		public void doCheck(){
			virtualPool.statusCheck(pool);
		}
		
		public int compareTo(Delayed other) {
			if (other == this) // compare zero ONLY if same object
                return 0;
			HeartbeatDelayed x = (HeartbeatDelayed)other;
            long diff = time - x.time;
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else if (sequenceNumber < x.sequenceNumber)
                return -1;
            else
                return 1;
		}

	    /**
	     * Returns nanosecond time offset by origin
	     */
	    final long now() {
	    	return System.nanoTime() - NANO_ORIGIN;
	    }
		
	}
	
	protected static final BlockingQueue<HeartbeatDelayed> HEART_BEAT_QUEUE = new DelayQueue<HeartbeatDelayed>();
	
	
	protected static class ObjectPoolStatus{
		static enum STATUS{INVALID,VALID};
		boolean inChecked = false;;
		STATUS status;
		final Lock lock = new ReentrantLock();
		long lastCheckTime = System.currentTimeMillis();
		ObjectPoolStatus(STATUS status){
			this.status = status;
		}
	}
	
	
	static{
		new Thread(){
			{
				this.setDaemon(true);
				this.setName("PoolHeartbeatThread");
			}
			public void run(){
				HeartbeatDelayed delayed = null;
				try {
					while(true){
						delayed = HEART_BEAT_QUEUE.take();
						ObjectPoolStatus status = delayed.virtualPool.statusCheck(delayed.pool);
						if(status.status == ObjectPoolStatus.STATUS.INVALID){
							delayed.setDelayedTime(5, TimeUnit.SECONDS);
							HEART_BEAT_QUEUE.offer(delayed);
						}else{
							status.lock.lock();
							try{
								status.inChecked = false;
							}finally{
								status.lock.unlock();
							}
						}
					}
				} catch (InterruptedException e) {
				}
			}
		}.start();
	}
	/**
	 * 负责均衡算法
	 */
	private int loadbalance;
	
	private AtomicLong currentCount = new AtomicLong(0);
	private ObjectPool[] objectPools;
	
	private ObjectPool[] runtimeObjectPools;
	
	private Map<ObjectPool,ObjectPoolStatus> poolStatusMap = new HashMap<ObjectPool,ObjectPoolStatus>();
	private ActiveNumComparator comparator = new ActiveNumComparator();
	
	public MultipleLoadBalanceObjectPool(){
	}
	
	public MultipleLoadBalanceObjectPool(int loadbalance,ObjectPool...objectPools){
		this.objectPools = objectPools;
		this.runtimeObjectPools = objectPools.clone();
		this.loadbalance = loadbalance;
		for(ObjectPool pool:objectPools){
			poolStatusMap.put(pool, new ObjectPoolStatus(ObjectPoolStatus.STATUS.VALID));
		}
	}
	
	public void setLoadbalance(int loadbalance) {
		this.loadbalance = loadbalance;
	}

	public void setObjectPools(ObjectPool[] objectPools) {
		this.objectPools = objectPools;
		this.runtimeObjectPools = objectPools.clone();
		poolStatusMap.clear();
		for(ObjectPool pool:objectPools){
			poolStatusMap.put(pool, new ObjectPoolStatus(ObjectPoolStatus.STATUS.VALID));
		}
	}
	
	public void addObject() throws Exception {
		throw new UnsupportedOperationException();
	}

	public Object borrowObject() throws Exception {
		ObjectPool pool= null;
		ObjectPool[] poolsTemp = runtimeObjectPools;
		if(poolsTemp.length == 0){
			throw new Exception("no valid pools");
		}
		
		if(loadbalance == LOADBALANCING_ROUNDROBIN){
			long current = currentCount.getAndIncrement();
			pool = poolsTemp[(int)(current % poolsTemp.length)];
		}else{
			if(poolsTemp.length >1){
				ObjectPool[] objectPoolsCloned = poolsTemp.clone();
				Arrays.sort(objectPoolsCloned, comparator);
				pool = objectPoolsCloned[0];
			}else if(poolsTemp.length == 1){
				pool = poolsTemp[0];
			}
		}
		
		try{
			return pool.borrowObject();
		}catch(Exception e){
			ObjectPoolStatus status = poolStatusMap.get(pool);
			
			status.lock.lock();
			try{
				if(!status.inChecked){
					status.inChecked = true;
					if(status.status == ObjectPoolStatus.STATUS.VALID){
						HEART_BEAT_QUEUE.offer(new HeartbeatDelayed(1,TimeUnit.MILLISECONDS,pool,this){
						});
					}
				}
			}finally{
				status.lock.unlock();
			}
			throw e;
		}
	}
	
	public void initAllPools(){
		for(ObjectPool pool : this.objectPools){
			ObjectPoolStatus status = statusCheck(pool);
			if(status.status == ObjectPoolStatus.STATUS.INVALID){
				HEART_BEAT_QUEUE.offer(new HeartbeatDelayed(2,TimeUnit.SECONDS,pool,this){
				});
			}
		}
	}
	
	/**
	 * 检测ObjectPool 是否能够正常提供 Object，并且将针对该Pool 对 runtimeObjectPools进行成员调整
	 * @param pool
	 * @return ObjectPoolStatus
	 */
	protected synchronized ObjectPoolStatus statusCheck(ObjectPool pool){
		ObjectPoolStatus status= this.poolStatusMap.get(pool);
		try {
			Object object = pool.borrowObject();
			pool.returnObject(object);
			//当前获得对象正常，如果前一状态是不可用的，则需要改变runtimeObjectPools成员。
			
			if(status.status == ObjectPoolStatus.STATUS.INVALID){
				status.status = ObjectPoolStatus.STATUS.VALID;
				ObjectPool[] pools = new ObjectPool[runtimeObjectPools.length+1];
				int index = 0;
				for(Map.Entry<ObjectPool, ObjectPoolStatus> entry : poolStatusMap.entrySet()){
					if(entry.getValue().status == ObjectPoolStatus.STATUS.VALID){
						pools[index ++] = entry.getKey();
					}
				}
				runtimeObjectPools = pools;
			}
		} catch (Exception e) {
			
			//如果无法获得对象，并且当前状态是可用的，则需要将该pool从 runtimeObjectPools中移出。
			if(status.status == ObjectPoolStatus.STATUS.VALID){
				status.status = ObjectPoolStatus.STATUS.INVALID;
				
				ObjectPool[] pools = new ObjectPool[runtimeObjectPools.length-1];
				int index = 0;
				for(Map.Entry<ObjectPool, ObjectPoolStatus> entry : poolStatusMap.entrySet()){
					if(entry.getValue().status == ObjectPoolStatus.STATUS.VALID){
						pools[index ++] = entry.getKey();
					}
				}
				runtimeObjectPools = pools;
			}
			
			
		}
		status.lastCheckTime = System.currentTimeMillis();
		return status;
	}

	public void clear() throws Exception, UnsupportedOperationException {
		for(ObjectPool pool : objectPools){
			pool.clear();
		}

	}

	public void close() throws Exception {
		for(ObjectPool pool : objectPools){
			pool.close();
		}
	}

	public int getNumActive() throws UnsupportedOperationException {
		int active = 0;
		for(ObjectPool pool : objectPools){
			active += pool.getNumActive();
		}
		return active;
	}

	public int getNumIdle() throws UnsupportedOperationException {
		int idle = 0;
		for(ObjectPool pool : objectPools){
			idle += pool.getNumIdle();
		}
		return idle;
	}

	public void invalidateObject(Object obj) throws Exception {
		PoolableObject poolableObject = (PoolableObject)obj;
		ObjectPool pool = poolableObject.getObjectPool();
		pool.invalidateObject(obj);
	}

	public void returnObject(Object obj) throws Exception {
		PoolableObject poolableObject = (PoolableObject)obj;
		ObjectPool pool = poolableObject.getObjectPool();
		pool.returnObject(obj);
	}

	public void setFactory(PoolableObjectFactory factory)
			throws IllegalStateException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
    
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean isEnabled) {
		this.enable = isEnabled;
	}

}
