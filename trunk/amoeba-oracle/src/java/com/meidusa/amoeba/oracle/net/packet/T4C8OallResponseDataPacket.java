package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.accessor.Accessor;
import com.meidusa.amoeba.oracle.handler.OracleQueryMessageHandler.ConnectionStatus;
import com.meidusa.amoeba.oracle.net.packet.assist.T4C8TTIrxh;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIdcb;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIoer;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIrxd;

/**
 * @author hexianmao
 * @version 2008-9-12 上午10:54:51
 */
public class T4C8OallResponseDataPacket extends DataPacket {

    static final byte                          QUERY_DESC   = 16;
    static final byte                          QUERY_RESULT = 6;
    static final byte                          QUERY_DATA   = 7;
    static final byte                          QUERY_NEXT   = 21;
    static final byte                          EXEC_RESULT  = 8;
    static final byte                          QUERY_END    = 4;

    private byte                               packetType;

    private T4CQueryDescResponseDataPacket     desc;
    private T4CQueryResultResponseDataPacket   query;
    private T4CExecuteResultResponseDataPacket execute;
    private ConnectionStatus                   connStatus;

    public T4C8OallResponseDataPacket(ConnectionStatus status){
        this.connStatus = status;
        this.connStatus.setCompleted(false);
        this.connStatus.setPacket(this);
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBufferExchanger meg = (T4CPacketBufferExchanger) buffer;
        packetType = meg.unmarshalSB1();
        // select 语句有两次数据包的交互，switch的状态为：16,8,4(返回字段描述) 和 6,(7,21,7,21,...),4(返回数据结果)
        // insert,update,delete 语句是一次数据包交互，switch的状态为：8,4
        switch (packetType) {
            case QUERY_DESC:// 16
                desc = new T4CQueryDescResponseDataPacket();
                desc.init(meg);
                break;
            case QUERY_RESULT:// 6
                query = new T4CQueryResultResponseDataPacket();
                query.init(meg);
                break;
            case EXEC_RESULT:// 8
                execute = new T4CExecuteResultResponseDataPacket();
                execute.init(meg);
                break;
            default:
                throw new RuntimeException("unknown type packet:" + packetType);
        }
        this.connStatus.setCompleted(true);
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        switch (packetType) {
            case QUERY_DESC:// 16
                desc.write2Buffer((T4CPacketBufferExchanger) buffer);
                break;
            case QUERY_RESULT:// 6
                query.write2Buffer((T4CPacketBufferExchanger) buffer);
                break;
            case EXEC_RESULT:// 8
                execute.write2Buffer((T4CPacketBufferExchanger) buffer);
                break;
            default:
                throw new RuntimeException("unknown type packet:" + packetType);
        }
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return T4CPacketBufferExchanger.class;
    }

    private class T4CQueryDescResponseDataPacket {

        T4CTTIdcb dcb = new T4CTTIdcb();

        int       len1;
        int[]     ai;
        int       len2;
        long[]    skip1;
        byte[][]  skip2;
        int[]     skip3;

        T4CTTIoer oer = new T4CTTIoer();

        protected void init(T4CPacketBufferExchanger meg) {
            // 16
            dcb.init(0);
            dcb.unmarshal(meg);
            connStatus.setNbOfCols(dcb.getNumuds());
            short[] dataType = new short[dcb.getNumuds()];
            for (int i = 0; i < dataType.length; i++) {
                dataType[i] = dcb.getUds()[i].getUdsoac().oacdty;
            }
            connStatus.setDataType(dataType);

            // 08
            meg.unmarshalSB1();
            len1 = meg.unmarshalUB2();
            ai = new int[len1];
            for (int l = 0; l < len1; l++) {
                ai[l] = (int) meg.unmarshalUB4();
            }
            meg.unmarshalUB2();
            len2 = meg.unmarshalUB2();
            skip1 = new long[len2];
            skip2 = new byte[len2][];
            skip3 = new int[len2];
            for (int k1 = 0; k1 < len2; k1++) {
                skip1[k1] = meg.unmarshalUB4();
                skip2[k1] = meg.unmarshalDALC();
                skip3[k1] = meg.unmarshalUB2();
            }

            // 04
            meg.unmarshalSB1();
            oer.init();
            oer.unmarshal(meg);
        }

        protected void write2Buffer(T4CPacketBufferExchanger meg) {
            // 16
            meg.marshalSB1(QUERY_DESC);
            dcb.marshal(meg, false);

            // 8
            meg.marshalSB1(EXEC_RESULT);
            meg.marshalUB2(len1);
            for (int l = 0; l < len1; l++) {
                meg.marshalUB4(ai[l]);
            }
            meg.marshalNULLPTR();
            meg.marshalUB2(len2);
            for (int k1 = 0; k1 < len2; k1++) {
                meg.marshalUB4(skip1[k1]);
                meg.marshalDALC(skip2[k1]);
                meg.marshalUB2(skip3[k1]);
            }

            // 4
            meg.marshalSB1(QUERY_END);
            oer.marshal(meg);
        }

    }

    private class T4CQueryResultResponseDataPacket {

        T4C8TTIrxh     rxh          = new T4C8TTIrxh();

        byte[][]       firstRow     = null;
        List<byte[]>   rowsIndicate = new ArrayList<byte[]>();
        List<byte[][]> rows         = new ArrayList<byte[][]>();

        T4CTTIoer      oer          = new T4CTTIoer();

        protected void init(T4CPacketBufferExchanger meg) {
            // 6
            rxh.init();
            rxh.unmarshalV10(meg);// rxd
            if (rxh.uacBufLength > 0) {
                throw new RuntimeException("invalid column type!");
            }

            // 7 or 4
            boolean flag = false;
            int colBit = (connStatus.getNbOfCols() / 8) + ((connStatus.getNbOfCols() % 8) == 0 ? 0 : 1);
            while (true) {
                byte byte0 = meg.unmarshalSB1();
                switch (byte0) {
                    case QUERY_DATA:// 7
                        if (flag) {
                            byte[][] row = new byte[connStatus.getNbOfCols()][];
                            row = unmarshalOneRow(row, null, meg);
                            rows.add(row);
                        } else {
                            firstRow = new byte[rxh.numRqsts][];
                            firstRow = unmarshalOneRow(firstRow, rxh.getIndicate(), meg);
                            flag = true;
                        }
                        break;
                    case QUERY_NEXT:// 21
                        int cols = meg.unmarshalUB2();
                        byte[] indicate = meg.getNBytes(colBit);
                        byte byte1 = meg.unmarshalSB1();
                        if (byte1 == QUERY_DATA) {
                            byte[][] row = new byte[cols][];
                            row = unmarshalOneRow(row, indicate, meg);
                            rowsIndicate.add(indicate);
                            rows.add(row);
                        } else {
                            throw new RuntimeException("protocol error," + byte1);
                        }
                        break;
                    case QUERY_END:// 4
                        oer.init();
                        oer.unmarshal(meg);
                        return;
                    default:
                        throw new RuntimeException("unknown type:" + byte0);
                }
            }
        }

        protected void write2Buffer(T4CPacketBufferExchanger meg) {
            // 6
            meg.marshalSB1(QUERY_RESULT);
            rxh.marshalV10(meg);

            // 7,21...
            if (firstRow != null) {
                meg.marshalSB1(QUERY_DATA);// 7,写入第一行数据
                marshalOneRow(firstRow, rxh.getIndicate(), meg);

                boolean writeIndicate = (rowsIndicate.size() == rows.size());
                for (int i = 0; i < rows.size(); i++) {
                    byte[][] abyte0 = rows.get(i);
                    if (writeIndicate) {
                        meg.marshalSB1(QUERY_NEXT);// 21
                        meg.marshalUB2(abyte0.length);// 列数
                        meg.writeBytes(rowsIndicate.get(i));// 指示数组
                    }
                    meg.marshalSB1(QUERY_DATA);// 7
                    marshalOneRow(abyte0, rowsIndicate.get(i), meg);
                }
            }

            // 4
            meg.marshalSB1(QUERY_END);
            oer.marshal(meg);
        }

        /**
         * 读取一行数据
         */
        private byte[][] unmarshalOneRow(byte[][] ab, byte[] indicate, T4CPacketBuffer meg) {
            int[] colsPos = T4CTTIrxd.getColsPosition(ab.length, indicate);
            for (int k = 0; k < ab.length; k++) {
                int pos = (colsPos == null) ? k : colsPos[k];
                switch (connStatus.getDataType()[pos]) {
                    case Accessor.CLOB:
                        ab[k] = meg.unmarshalDALC();
                        break;
                    default:
                        ab[k] = meg.unmarshalCLRforREFS();
                }
            }
            return ab;
        }

        /**
         * 写入一行数据
         */
        private void marshalOneRow(byte[][] ab, byte[] indicate, T4CPacketBuffer meg) {
            int[] colsPos = T4CTTIrxd.getColsPosition(ab.length, indicate);
            for (int k = 0; k < ab.length; k++) {
                if (ab[k] == null || ab.length == 0) {
                    meg.writeByte((byte) 0);
                } else {
                    int pos = (colsPos == null) ? k : colsPos[k];
                    switch (connStatus.getDataType()[pos]) {
                        case Accessor.CLOB:
                            meg.marshalDALC(ab[k]);
                            break;
                        default:
                            meg.marshalCLR(ab[k], ab[k].length);
                    }
                }
            }
        }
    }

    private class T4CExecuteResultResponseDataPacket {

        int       len1;
        int[]     ai;
        int       len2;
        long[]    skip1;
        byte[][]  skip2;
        int[]     skip3;

        T4CTTIoer oer = new T4CTTIoer();

        protected void init(T4CPacketBufferExchanger meg) {
            // 08
            len1 = meg.unmarshalUB2();
            ai = new int[len1];
            for (int l = 0; l < len1; l++) {
                ai[l] = (int) meg.unmarshalUB4();
            }

            meg.unmarshalUB2();// skip

            len2 = meg.unmarshalUB2();
            skip1 = new long[len2];
            skip2 = new byte[len2][];
            skip3 = new int[len2];
            for (int k1 = 0; k1 < len2; k1++) {
                skip1[k1] = meg.unmarshalUB4();
                skip2[k1] = meg.unmarshalDALC();
                skip3[k1] = meg.unmarshalUB2();
            }

            // 04
            meg.unmarshalSB1();
            oer.init();
            oer.unmarshal(meg);
        }

        protected void write2Buffer(T4CPacketBufferExchanger meg) {
            // 8
            meg.marshalSB1(EXEC_RESULT);
            meg.marshalUB2(len1);
            for (int l = 0; l < len1; l++) {
                meg.marshalUB4(ai[l]);
            }

            meg.marshalNULLPTR();// skip

            meg.marshalUB2(len2);
            for (int k1 = 0; k1 < len2; k1++) {
                meg.marshalUB4(skip1[k1]);
                meg.marshalDALC(skip2[k1]);
                meg.marshalUB2(skip3[k1]);
            }

            // 4
            meg.marshalSB1(QUERY_END);
            oer.marshal(meg);
        }

    }

    // ///////////////////////////////////////////////////////////////////////////////////

    public static boolean isParseable(byte[] message) {
        if (isDataType(message) && message.length >= 11) {
            int flag = (message[10] & 0xff);
            if (flag == QUERY_DESC || flag == QUERY_RESULT || flag == EXEC_RESULT) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPacketEOF(byte[] message) {
        return true;
    }

}
