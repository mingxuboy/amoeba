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

public interface ObjectPool extends org.apache.commons.pool.ObjectPool {
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
                try {
                    while (true) {
                        delayed = HEART_BEAT_QUEUE.take();
                        STATUS status = delayed.doCheck();
                        if (status == STATUS.INVALID) {
                            delayed.setDelayedTime(5, TimeUnit.SECONDS);
                            HeartbeatManager.addPooltoHeartbeat(delayed);
                        }else{
                        	delayed.pool.afterChecked(delayed.pool);
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        }.start();
    	}
    	
    	public static void addPooltoHeartbeat(HeartbeatDelayed delay){
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
        private long                          NANO_ORIGIN = System.nanoTime();
        private static final AtomicLong       sequencer   = new AtomicLong(0);
        private ObjectPool                    pool;

        public ObjectPool getPool() {
			return pool;
		}

		public HeartbeatDelayed(long nsTime, TimeUnit timeUnit, ObjectPool pool){
            this.time = TimeUnit.NANOSECONDS.convert(nsTime, timeUnit);
            this.pool = pool;
            this.sequenceNumber = sequencer.getAndIncrement();
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
            NANO_ORIGIN = System.nanoTime();
            this.time = TimeUnit.NANOSECONDS.convert(time, timeUnit);
        }

        public long getDelay(TimeUnit unit) {
            long d = unit.convert(time - now(), TimeUnit.NANOSECONDS);
            return d;
        }

/*        protected synchronized ObjectPoolStatus statusCheck(ObjectPool pool) {
            ObjectPoolStatus status = HearbeatManager.poolStatusMap.get(pool);
            try {
                Object object = pool.borrowObject();
                pool.returnObject(object);
                // 当前获得对象正常，如果前一状态是不可用的，则需要改变runtimeObjectPools成员。

                if (status.status == ObjectPoolStatus.STATUS.INVALID) {
                    status.status = ObjectPoolStatus.STATUS.VALID;
                    ObjectPool[] pools = new ObjectPool[runtimeObjectPools.length + 1];
                    int index = 0;
                    for (Map.Entry<ObjectPool, ObjectPoolStatus> entry : HearbeatManager.poolStatusMap.entrySet()) {
                        if (entry.getValue().status == ObjectPoolStatus.STATUS.VALID) {
                            pools[index++] = entry.getKey();
                        }
                    }
                    runtimeObjectPools = pools;
                }
            } catch (Exception e) {

                // 如果无法获得对象，并且当前状态是可用的，则需要将该pool从 runtimeObjectPools中移出。
                if (status.status == ObjectPoolStatus.STATUS.VALID) {
                    status.status = ObjectPoolStatus.STATUS.INVALID;

                    ObjectPool[] pools = new ObjectPool[runtimeObjectPools.length - 1];
                    int index = 0;
                    for (Map.Entry<ObjectPool, ObjectPoolStatus> entry : poolStatusMap.entrySet()) {
                        if (entry.getValue().status == ObjectPoolStatus.STATUS.VALID) {
                            pools[index++] = entry.getKey();
                        }
                    }
                    runtimeObjectPools = pools;
                }

            }
            status.lastCheckTime = System.currentTimeMillis();
            return status;
        }*/
        
        public STATUS doCheck() {
        	Object object = null;
			try {
				object = pool.borrowObject();
				pool.setValid(true);
				return STATUS.VALID;
			} catch (Exception e) {
				pool.setValid(false);
				return STATUS.INVALID;
			}finally{
				if(object != null){
					try {
						pool.returnObject(object);
					} catch (Exception e) {
					}
				}
			}
        }

        public int compareTo(Delayed other) {
            if (other == this) // compare zero ONLY if same object
            return 0;
            HeartbeatDelayed x = (HeartbeatDelayed) other;
            long diff = time - x.time;
            if (diff < 0) return -1;
            else if (diff > 0) return 1;
            else if (sequenceNumber < x.sequenceNumber) return -1;
            else return 1;
        }

        /**
         * Returns nanosecond time offset by origin
         */
        final long now() {
            return System.nanoTime() - NANO_ORIGIN;
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
	
	public void afterChecked(ObjectPool pool);
}
