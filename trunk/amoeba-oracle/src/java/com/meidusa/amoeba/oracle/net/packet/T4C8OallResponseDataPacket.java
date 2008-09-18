package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.accessor.Accessor;
import com.meidusa.amoeba.oracle.net.packet.assist.T4C8TTIrxh;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIdcb;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIfob;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIiov;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIoer;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIrxd;

/**
 * @author hexianmao
 * @version 2008-9-12 上午10:54:51
 */
public class T4C8OallResponseDataPacket extends DataPacket {

    static final int QUERY_DESC   = 16;
    static final int QUERY_RESULT = 6;
    static final int EXEC_RESULT  = 8;

    int              cursor;
    long             rowsProcessed;

    boolean          isLastPacket = false;
    boolean          isCompleted  = false;

    T4CTTIoer        oer          = new T4CTTIoer();
    T4C8TTIrxh       rxh          = new T4C8TTIrxh();
    T4CTTIrxd        rxd          = new T4CTTIrxd();
    T4CTTIdcb        dcb          = new T4CTTIdcb();
    T4CTTIfob        fob          = new T4CTTIfob();

    Accessor[]       definesAccessors;
    Accessor[]       outBindAccessors;

    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        boolean flag = false;
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        byte byte0 = meg.unmarshalSB1();
        if (byte0 == QUERY_RESULT || byte0 == EXEC_RESULT) {
            isLastPacket = true;
        }
        while (true) {
            // select 语句有两次数据包的交互，switch的状态为：16,8,4(返回字段描述) 和 6,(7,21,7,21,...),4(返回数据结果)
            // insert,update,delete 语句是一次数据包交互，switch的状态为：8,4
            switch (byte0) {
                case 4:// 数据结束返回结果描述
                    oer.init();
                    oer.unmarshal(meg);
                    cursor = oer.currCursorID;
                    rowsProcessed = oer.curRowNumber;
                    if (oer.retCode != 1403) {
                        // oer.processError(oracleStatement);
                    }
                    if (isLastPacket) {
                        isCompleted = true;
                    }
                    return;
                case QUERY_RESULT:// select 开始返回查询数据
                    rxh.init();
                    rxh.unmarshalV10(rxd, meg);
                    if (rxh.uacBufLength > 0) {
                        throw new RuntimeException("无效的列类型");
                    }
                    break;
                case 7:// select 返回一行数据结果
                    for (int k = 0; k < rxh.numRqsts; k++) {
                        meg.unmarshalCLRforREFS();
                    }
                    break;
                case EXEC_RESULT:// insert,update,delete 返回描述
                    if (flag) {
                        throw new RuntimeException("protocol error");
                    }
                    int j = meg.unmarshalUB2();
                    int ai[] = new int[j];
                    for (int l = 0; l < j; l++) {
                        ai[l] = (int) meg.unmarshalUB4();
                    }
                    cursor = ai[2];
                    meg.unmarshalUB2();
                    int i1 = meg.unmarshalUB2();
                    if (i1 > 0) {
                        for (int k1 = 0; k1 < i1; k1++) {
                            meg.unmarshalUB4();
                            meg.unmarshalDALC();
                            meg.unmarshalUB2();
                        }
                    }
                    flag = true;
                    break;
                case 11:
                    T4CTTIiov iov = new T4CTTIiov(rxh, rxd);
                    iov.init();
                    iov.unmarshalV10(meg);

                    // if (!iov.isIOVectorEmpty()) {
                    // byte abyte0[] = iov.getIOVector();
                    // iov.processRXD(outBindAccessors, numberOfBindPositions, bindBytes, bindChars, bindIndicators,
                    // bindIndicatorSubRange, conversion, tmpBindsByteArray, abyte0, parameterStream, parameterDatum,
                    // parameterOtype, oracleStatement, null, null, null);
                    // }

                    // break;
                    throw new RuntimeException("unknown result switch type:11 in T4C8OallResponseDataPacket.");
                case QUERY_DESC:// select 返回查询字段的描述结果
                    dcb.init(0);
                    definesAccessors = dcb.receive(definesAccessors, meg);
                    rxd.setNumberOfColumns(dcb.getNumuds());
                    break;
                case 19:
                    fob.marshal(meg);
                    break;
                case 21:// select 继续返回下一行数据
                    int i = meg.unmarshalUB2();
                    rxd.unmarshalBVC(i, meg);
                    break;
                default:
                    throw new RuntimeException("protocol error");
            }
            byte0 = meg.unmarshalSB1();
        }
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return T4CPacketBuffer.class;
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    public static boolean isParseable(byte[] message) {
        return true;
    }

    public static boolean isPacketEOF(byte[] message) {
        return true;
    }

}
