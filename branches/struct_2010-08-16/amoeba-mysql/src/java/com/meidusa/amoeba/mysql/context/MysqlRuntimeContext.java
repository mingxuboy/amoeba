package com.meidusa.amoeba.mysql.context;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.map.LRUMap;

import com.meidusa.amoeba.context.RuntimeContext;
import com.meidusa.amoeba.heartbeat.HeartbeatDelayed;
import com.meidusa.amoeba.heartbeat.HeartbeatManager;
import com.meidusa.amoeba.heartbeat.Status;
import com.meidusa.amoeba.mysql.net.packet.OKforPreparedStatementPacket;
import com.meidusa.amoeba.mysql.util.CharsetMapping;
import com.meidusa.amoeba.util.InitialisationException;

public class MysqlRuntimeContext extends RuntimeContext {
	public final static String SERVER_VERSION = "5.1.45-mysql-amoeba-proxy-2.0.0-BETA";
	private byte               serverCharsetIndex;
	private Map<String,OKforPreparedStatementPacket> preparedMap = null;
	private int statementCacheSize = 10000;
	private long statementExpiredTime = 5;
    public void setServerCharsetIndex(byte serverCharsetIndex) {
        this.serverCharsetIndex = serverCharsetIndex;
        this.setServerCharset(CharsetMapping.INDEX_TO_CHARSET[serverCharsetIndex & 0xff]);
    }

    public byte getServerCharsetIndex() {
        if (serverCharsetIndex > 0) return serverCharsetIndex;
        return CharsetMapping.getCharsetIndex(this.getServerCharset());
    }
    
    public int getStatementCacheSize() {
		return statementCacheSize;
	}

	public void setStatementCacheSize(int statementCacheSize) {
		this.statementCacheSize = statementCacheSize;
	}

	public long getStatementExpiredTime() {
		return statementExpiredTime;
	}

	public void setStatementExpiredTime(long statementExpiredTime) {
		this.statementExpiredTime = statementExpiredTime;
	}

	@SuppressWarnings("unchecked")
	public void init() throws InitialisationException {
    	super.init();
    	preparedMap = Collections.synchronizedMap(new LRUMap(statementCacheSize){
    		protected boolean removeLRU(LinkEntry entry){
    			boolean result = super.removeLRU(entry);
    			//entry.
    			return result;
    		}
    	});
    	HeartbeatManager.addHeartbeat(new HeartbeatDelayed(statementExpiredTime, TimeUnit.MINUTES){

			@Override
			public Status doCheck() {
				preparedMap.clear();
				return Status.VALID;
			}

			@Override
			public String getName() {
				return "StateMent Cleaner";
			}
			
			public boolean isCycle(){
	        	return true;
	        }
    		
    	});
    }
}
