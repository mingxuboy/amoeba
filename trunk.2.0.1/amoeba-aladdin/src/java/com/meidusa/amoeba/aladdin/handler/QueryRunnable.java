package com.meidusa.amoeba.aladdin.handler;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.result.ResultPacket;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.PoolableObject;

/**
 * @author struct
 * @author hexianmao
 * @version 2008-11-24 上午10:38:34
 */
public abstract class QueryRunnable implements MessageHandlerRunner {

    private static Logger           logger = Logger.getLogger(QueryRunnable.class);

    protected Object                parameter;
    protected ResultPacket          packet;
    protected String                query;
    protected MysqlClientConnection source;

    private CountDownLatch          latch;
    private PoolableObject          conn;

    public QueryRunnable(CountDownLatch latch, PoolableObject conn, String query, Object parameter, ResultPacket packet){
        this.latch = latch;
        this.conn = conn;
        this.query = query;
        this.parameter = parameter;
        this.packet = packet;
    }

    public void init(MessageHandler handler) {
        this.source = ((CommandMessageHandler) handler).source;
    }

    protected static boolean isSelect(String query) {
        char ch = query.trim().charAt(0);
        if (ch == 's' || ch == 'S') {
            return true;
        } else {
            return false;
        }
    }

    /**
     * PoolableObject 将在end session 中返回到pool中
     */
    protected abstract void doRun(PoolableObject conn) throws Exception;

    public void run() {
        try {
            try {
                doRun(conn);
            } catch (Exception e) {
                logger.error("run query error:", e);
            }
        } finally {
            if (latch != null) {
                latch.countDown();
            }
            reset();
        }
    }

    public void reset() {
        source = null;
        query = null;
        packet = null;
        latch = null;
        parameter = null;
        conn = null;
    }

}
