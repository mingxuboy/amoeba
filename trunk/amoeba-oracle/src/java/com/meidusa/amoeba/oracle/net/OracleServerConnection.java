package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;

public class OracleServerConnection extends OracleConnection implements PoolableObject {

    private boolean    active;
    private ObjectPool objectPool;

    public OracleServerConnection(SocketChannel channel, long createStamp){
        super(channel, createStamp);
    }

    
    public ObjectPool getObjectPool() {
        return objectPool;
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void setObjectPool(ObjectPool pool) {
        this.objectPool = pool;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRemovedFromPool() {
        return objectPool == null;
    }

    protected void close(Exception exception) {
        super.close(exception);
        final ObjectPool tmpPool = objectPool;
        objectPool = null;
        try {
            if (tmpPool != null) {

                /**
                 * 处于active 状态的 poolableObject，可以用ObjectPool.invalidateObject 方式从pool中销毁 否则只能等待被borrow 或者 idle time out
                 */
                if (isActive()) {
                    tmpPool.invalidateObject(this);
                }
            }
        } catch (Exception e) {

        }
    }

}
