package com.meidusa.amoeba.aladdin.handler;

import java.util.concurrent.CountDownLatch;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.handler.PreparedStatmentInfo;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.result.PreparedResultPacket;
import com.meidusa.amoeba.mysql.net.packet.result.ResultPacket;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.parser.ParseException;
import com.meidusa.amoeba.route.SqlBaseQueryRouter;

/**
 * @author struct
 * @author hexianmao
 */
public class PreparedStatmentMessageHandler extends CommandMessageHandler {

    protected static class PreparedQueryRunnable extends QueryRunnable {

        protected PreparedQueryRunnable(CountDownLatch latch, PoolableObject conn, String query, Object parameter,
                                        ResultPacket packet){
            super(latch, conn, query, parameter, packet);
        }

        @Override
        protected void doRun(PoolableObject conn) throws ParseException{
        	SqlBaseQueryRouter router = (SqlBaseQueryRouter)ProxyRuntimeContext.getInstance().getQueryRouter();
            int count = router.parseParameterCount((DatabaseConnection) this.source, query);
            PreparedResultPacket preparedPacket = (PreparedResultPacket) packet;
            PreparedStatmentInfo preparedInfo = (PreparedStatmentInfo) parameter;
            preparedPacket.setStatementId(preparedInfo.getStatmentId());
            preparedPacket.setParameterCount(count);
        }
    }

    public PreparedStatmentMessageHandler(MysqlClientConnection conn, PreparedStatmentInfo preparedInf,
                                          ObjectPool[] pools, long timeout){
        super(conn, preparedInf.getSql(), preparedInf, pools, timeout);
    }

    @Override
    protected QueryRunnable newQueryRunnable(CountDownLatch latch, PoolableObject conn, String query, Object parameter,
                                             ResultPacket packet) {
        return new PreparedQueryRunnable(latch, conn, query, parameter, packet);
    }

    @Override
    protected ResultPacket newResultPacket(String query) {
        PreparedResultPacket resultPacket = new PreparedResultPacket();
        return resultPacket;
    }

}
