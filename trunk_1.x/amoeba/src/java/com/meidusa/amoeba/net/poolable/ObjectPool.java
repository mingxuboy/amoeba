/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.meidusa.amoeba.net.poolable;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;


public interface ObjectPool extends org.apache.commons.pool.ObjectPool {
	static Logger logger = Logger.getLogger(ObjectPool.class); 
	static enum STATUS {
		INVALID, VALID
	};
    static class HeartbeatManager{
    	protected static final BlockingQueue<HeartbeatDelayed> HEART_BEAT_QUEUE = new DelayQueue<HeartbeatDelayed>();
    	static{
        new Thread() {
            {
                this.setDaemon(true);
                this.setName("HeartbeatManagerThread");
            }

            public void run() {
                HeartbeatDelayed delayed = null;
                while (true) {
                	try{
                        delayed = HEART_BEAT_QUEUE.take();
                        STATUS status = delayed.doCheck();
                        if(logger.isDebugEnabled()){
                        	logger.debug("checked Pool poolName="+delayed.getPool().getName()+" ,Status="+status);
                        }
                        if (status == STATUS.INVALID) {
                            //delayed.setDelayedTime(5, TimeUnit.SECONDS);
                        	if(!delayed.isCycle()){
                        		delayed.reset();
                        		HeartbeatManager.addHeartbeat(delayed);
                        	}
                        }
                        
                        if(delayed.isCycle()){
                        	delayed.reset();
                        	HeartbeatManager.addHeartbeat(delayed);
                        }
                	}catch(Exception e){
                		logger.error("check pool error",e);
                	}
                }
            }
        }.start();
    	}
    	
    	public static void addHeartbeat(HeartbeatDelayed delay){
    		if(!HEART_BEAT_QUEUE.contains(delay)){
    			HEART_BEAT_QUEUE.offer(delay);
    		}
    	}
    }

    public static class ActiveNumComparator implements Comparator<ObjectPool> {

        public int compare(ObjectPool o1, ObjectPool o2) {
            return o1.getNumActive() - o2.getNumActive();
        }
    }

	public static class HeartbeatDelayed implements Delayed {

        private long                          time;
        /** Sequence number to break ties FIFO */
        private final long                    sequenceNumber;
        private long                          nano_origin = System.nanoTime();
        private static final AtomicLong       sequencer   = new AtomicLong(0);
        private ObjectPool                    pool;
        private long nextFireTime = nano_origin;
        
        public boolean isCycle(){
        	return false;
        }
        
        public ObjectPool getPool() {
			return pool;
		}

		public HeartbeatDelayed(long nsTime, TimeUnit timeUnit, ObjectPool pool){
            this.time = TimeUnit.NANOSECONDS.convert(nsTime, timeUnit);
            this.pool = pool;
            this.sequenceNumber = sequencer.getAndIncrement();
            nextFireTime = time + nano_origin;
        }

	    public boolean equals(Object obj) {
	    	if(obj instanceof HeartbeatDelayed){
	    		HeartbeatDelayed other = (HeartbeatDelayed)obj;
	    		return other.pool == this.pool && this.getClass() == obj.getClass();
	    	}else{
	    		return false;
	    	}
        }
	    
	    public int hashCode(){
	    	return pool == null?this.getClass().hashCode():this.getClass().hashCode() + pool.hashCode();
	    }
	    
        public void setDelayedTime(long time, TimeUnit timeUnit) {
            nano_origin = System.nanoTime();
            this.time = TimeUnit.NANOSECONDS.convert(time, timeUnit);
            nextFireTime = time + nano_origin;
        }
        
        public void reset(){
        	nano_origin = System.nanoTime();
        	nextFireTime = time + nano_origin;
        }

        public long getDelay(TimeUnit unit) {
            long d = unit.convert(time - now(), TimeUnit.NANOSECONDS);
            return d;
        }
        
        public STATUS doCheck() {
			if(pool.validate()){
				pool.setValid(true);
				return STATUS.VALID;
			}else{
				pool.setValid(false);
				return STATUS.INVALID;
			}
        }

        public int compareTo(Delayed other) {
            if (other == this) // compare zero ONLY if same object
            return 0;
            HeartbeatDelayed x = (HeartbeatDelayed) other;
            long diff = nextFireTime - x.nextFireTime;
            if (diff < 0) return -1;
            else if (diff > 0) return 1;
            else if (sequenceNumber < x.sequenceNumber) return -1;
            else return 1;
        }

        /**
         * Returns nanosecond time offset by origin
         */
        final long now() {
            return System.nanoTime() - nano_origin;
        }

	}

	/**
	 * return this pool enabled/disabled status
	 * 
	 * @return
	 */
	boolean isEnable();

	void setEnable(boolean isEnabled);

	boolean isValid();
	
	void setValid(boolean valid);
	
	public boolean validate();
	
	public String getName();
	
	public void setName(String name);
}
