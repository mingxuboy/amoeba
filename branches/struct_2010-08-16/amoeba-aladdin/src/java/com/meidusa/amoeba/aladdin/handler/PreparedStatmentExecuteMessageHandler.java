package com.meidusa.amoeba.aladdin.handler;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.aladdin.util.ResultSetUtil;
import com.meidusa.amoeba.jdbc.PoolableJdbcConnection;
import com.meidusa.amoeba.mysql.handler.PreparedStatmentInfo;
import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.BindValue;
import com.meidusa.amoeba.mysql.net.packet.ExecutePacket;
import com.meidusa.amoeba.mysql.net.packet.result.MysqlResultSetPacket;
import com.meidusa.amoeba.mysql.net.packet.result.MysqlSimpleResultPacket;
import com.meidusa.amoeba.mysql.net.packet.result.ResultPacket;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;

/**
 * @author struct
 * @author hexianmao
 */

public class PreparedStatmentExecuteMessageHandler extends CommandMessageHandler {

    private ExecutePacket packet;

    protected static class PreparedExecuteQueryRunnable extends QueryRunnable {

        private static Logger logger = Logger.getLogger(PreparedExecuteQueryRunnable.class);

        private ExecutePacket executePacket;

        PreparedExecuteQueryRunnable(CountDownLatch latch, PoolableObject conn, String query, Object parameter,
                                     ResultPacket packet){
            super(latch, conn, query, parameter, packet);
        }

        @Override
        public void init(MessageHandler handler) {
            super.init(handler);
            executePacket = ((PreparedStatmentExecuteMessageHandler) handler).packet;
        }

        @Override
        protected void doRun(PoolableObject conn) {
            PreparedStatement pst = null;
            ResultSet rs = null;
            try {
                pst = ((java.sql.Connection) conn).prepareStatement(query);
                int i = 1;
                for (BindValue bindValue : executePacket.values) {
                    if (!bindValue.isNull) {
                        switch (bindValue.bufferType) {
                            case MysqlDefs.FIELD_TYPE_TINY:
                                pst.setByte(i++, bindValue.byteBinding);
                                break;
                            case MysqlDefs.FIELD_TYPE_SHORT:
                                pst.setShort(i++, bindValue.shortBinding);
                                break;
                            case MysqlDefs.FIELD_TYPE_LONG:
                                pst.setLong(i++, bindValue.longBinding);
                                break;
                            case MysqlDefs.FIELD_TYPE_LONGLONG:
                                pst.setLong(i++, bindValue.longBinding);
                                break;
                            case MysqlDefs.FIELD_TYPE_FLOAT:
                                pst.setFloat(i++, bindValue.floatBinding);
                                break;
                            case MysqlDefs.FIELD_TYPE_DOUBLE:
                                pst.setDouble(i++, bindValue.doubleBinding);
                                break;
                            case MysqlDefs.FIELD_TYPE_TIME:
                                pst.setTime(i++, (Time) bindValue.value);
                                break;
                            case MysqlDefs.FIELD_TYPE_DATE:
                            case MysqlDefs.FIELD_TYPE_DATETIME:
                            case MysqlDefs.FIELD_TYPE_TIMESTAMP:
                                java.util.Date date = (java.util.Date) bindValue.value;
                                pst.setDate(i++, new Date(date.getTime()));
                                break;
                            case MysqlDefs.FIELD_TYPE_VAR_STRING:
                            case MysqlDefs.FIELD_TYPE_STRING:
                            case MysqlDefs.FIELD_TYPE_VARCHAR:
                                pst.setString(i++, (String) bindValue.value);
                                break;
                            case MysqlDefs.FIELD_TYPE_DECIMAL:
                            case MysqlDefs.FIELD_TYPE_NEW_DECIMAL:
                                pst.setBigDecimal(i++, (BigDecimal) bindValue.value);
                                break;
                            default: {
                                logger.error("error type=" + bindValue.bufferType + " index=" + i);
                            }
                        }
                    } else {
                        pst.setObject(i++, null);
                    }
                }
                if (isSelect(query)) {
                    rs = pst.executeQuery();
                    MysqlResultSetPacket resultPacket = (MysqlResultSetPacket) packet;
                    PoolableJdbcConnection poolableJdbcConnection = (PoolableJdbcConnection) conn;
                    ResultSetUtil.resultSetToPacket(source, resultPacket, rs, poolableJdbcConnection.getResultSetHandler());
                } else {
                    MysqlSimpleResultPacket simplePacket = (MysqlSimpleResultPacket) packet;
                    simplePacket.addResultCount(pst.executeUpdate());
                }
            } catch (SQLException e) {
                logger.error("execute error", e);
                packet.setError(e.getErrorCode(), e.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                }

                if (pst != null) {
                    try {
                        pst.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
    }

    public PreparedStatmentExecuteMessageHandler(MysqlClientConnection conn, PreparedStatmentInfo pInfo,
                                                 ExecutePacket packet, ObjectPool[] pools, long timeout){
        super(conn, pInfo.getSql(), pInfo, pools, timeout);
        this.packet = packet;
    }

    @Override
    protected QueryRunnable newQueryRunnable(CountDownLatch latch, PoolableObject conn, String query, Object parameter,
                                             ResultPacket packet) {
        return new PreparedExecuteQueryRunnable(latch, conn, query, parameter, packet);
    }

    @Override
    protected ResultPacket newResultPacket(String query) {
        if (PreparedExecuteQueryRunnable.isSelect(query)) {
            MysqlResultSetPacket packet = new MysqlResultSetPacket(query);
            packet.setPrepared(true);
            return packet;
        } else {
            return new MysqlSimpleResultPacket();
        }
    }

}
