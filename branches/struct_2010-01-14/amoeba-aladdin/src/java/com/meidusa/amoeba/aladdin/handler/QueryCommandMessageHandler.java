package com.meidusa.amoeba.aladdin.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.aladdin.util.ResultSetUtil;
import com.meidusa.amoeba.jdbc.PoolableJdbcConnection;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.result.MysqlResultSetPacket;
import com.meidusa.amoeba.mysql.net.packet.result.MysqlSimpleResultPacket;
import com.meidusa.amoeba.mysql.net.packet.result.ResultPacket;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;

/**
 * @author struct
 * @author hexianmao
 */
public class QueryCommandMessageHandler extends CommandMessageHandler {

    protected static class QueryCommandRunnable extends QueryRunnable {

        private static Logger logger = Logger.getLogger(QueryCommandRunnable.class);

        public QueryCommandRunnable(CountDownLatch latch, PoolableObject conn, String query, Object parameter,
                                    ResultPacket packet){
            super(latch, conn, query, parameter, packet);
        }

        @Override
        protected void doRun(PoolableObject conn) {
            if (isSelect(query)) {
                Statement statement = null;
                ResultSet rs = null;
                try {
                    statement = ((java.sql.Connection) conn).createStatement();
                    rs = statement.executeQuery(query);
                    if (logger.isDebugEnabled()) {
                        logger.debug("starting query:" + query);
                    }
                    PoolableJdbcConnection poolableJdbcConnection = (PoolableJdbcConnection) conn;
                    ResultSetUtil.resultSetToPacket(source, (MysqlResultSetPacket) packet, rs, poolableJdbcConnection.getResultSetHandler());
                } catch (SQLException e) {
                    packet.setError(e.getErrorCode(), e.getMessage());
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                        }
                    }

                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                        }
                    }
                }
            } else {
                Statement statement = null;
                ResultSet rs = null;
                try {
                    statement = ((java.sql.Connection) conn).createStatement();
                    ((MysqlSimpleResultPacket) packet).addResultCount(statement.executeUpdate(query));
                } catch (SQLException e) {
                    packet.setError(e.getErrorCode(), e.getMessage());
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                        }
                    }

                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                        }
                    }
                }
            }
        }
    }

    public QueryCommandMessageHandler(MysqlClientConnection source, String query, Object parameter, ObjectPool[] pools,
                                      long timeout){
        super(source, query, parameter, pools, timeout);
    }

    @Override
    public QueryRunnable newQueryRunnable(CountDownLatch latch, PoolableObject conn, String query, Object parameter,
                                          ResultPacket packet) {
        return new QueryCommandRunnable(latch, conn, query, parameter, packet);
    }

    @Override
    protected ResultPacket newResultPacket(String query) {
        if (QueryRunnable.isSelect(query)) {
            return new MysqlResultSetPacket(query);
        } else {
            return new MysqlSimpleResultPacket();
        }
    }

}
