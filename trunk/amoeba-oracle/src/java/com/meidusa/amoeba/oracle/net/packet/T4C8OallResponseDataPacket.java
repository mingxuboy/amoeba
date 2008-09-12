package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIoer;

/**
 * @author hexianmao
 * @version 2008-9-12 上午10:54:51
 */
public class T4C8OallResponseDataPacket extends DataPacket {

    T4CTTIoer oer;
    int       cursor;
    long      rowsProcessed;
    int       receiveState;

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        while (true) {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 4:
                    oer.init();
                    oer.unmarshal(meg);
                    cursor = oer.currCursorID;
                    rowsProcessed = oer.curRowNumber;
                    if (oer.retCode != 1403) {
                        try {
                            // oer.processError(oracleStatement);
                        } catch (Exception e) {
                            receiveState = 0;
                        }
                    }

                    if (receiveState != 1) {
                        throw new RuntimeException("OALL8 处于不一致状态");
                    }
                    receiveState = 0;
                    return;
                case 6:
                    // rxh.init();
                    // rxh.unmarshalV10(rxd);
                    // if (rxh.uacBufLength > 0) {
                    // DatabaseError.throwSqlException(405);
                    // }
                    // flag1 = true;
                    break;
                case 7:// _L4
                    // if (receiveState != 1) {
                    // DatabaseError.throwSqlException(447);
                    // }
                    // receiveState = 2;
                    // if (oracleStatement.returnParamAccessors == null || numberOfBindPositions <= 0) {// L49
                    // if (!flag3 && (outBindAccessors == null || definesAccessors != null)) {
                    // if (!rxd.unmarshal(definesAccessors, definesLength)) {
                    // receiveState = 1;
                    // break;
                    // } else {
                    // receiveState = 3;
                    // meg.sentCancel = false;
                    // meg.pipeState = -1;
                    // return;
                    // }
                    // } else {
                    // if (!rxd.unmarshal(outBindAccessors, numberOfBindPositions)) {
                    // receiveState = 1;
                    // break;
                    // } else {
                    // receiveState = 3;
                    // meg.sentCancel = false;
                    // meg.pipeState = -1;
                    // return;
                    // }
                    // }
                    // } else {// L50
                    // boolean flag4 = false;// 7
                    // for (int k = 0; k < oracleStatement.numberOfBindPositions; k++) {// L52
                    // Accessor accessor = oracleStatement.returnParamAccessors[k];// 9// L54
                    // if (accessor == null) {
                    // continue;
                    // }
                    // int j1 = (int) meg.unmarshalUB4();// 10// L56
                    // if (!flag4) {
                    // // L61
                    // oracleStatement.rowsDmlReturned = j1;
                    // oracleStatement.allocateDmlReturnStorage();
                    // oracleStatement.setupReturnParamAccessors();
                    // flag4 = true;
                    // }
                    // // L60
                    // for (int l1 = 0; l1 < j1; l1++) {
                    // accessor.unmarshalOneRow();// L66
                    // }
                    // }
                    // oracleStatement.returnParamsFetched = true;// L53
                    // receiveState = 1;// L8
                    // break;
                    // }
                case 8:// _L5
                    // if (flag) {
                    // DatabaseError.throwSqlException(401);
                    // }
                    // int j = meg.unmarshalUB2();
                    // int ai[] = new int[j];
                    // for (int l = 0; l < j; l++) {
                    // ai[l] = (int) meg.unmarshalUB4();
                    // }
                    // cursor = ai[2];
                    // meg.unmarshalUB2();
                    // int i1 = meg.unmarshalUB2();
                    // if (i1 > 0) {
                    // for (int k1 = 0; k1 < i1; k1++) {
                    // int i2 = (int) meg.unmarshalUB4();
                    // meg.unmarshalDALC();
                    // int j2 = meg.unmarshalUB2();
                    // }
                    // }
                    // flag = true;
                    break;
                case 11:// _L6
                    // T4CTTIiov t4cttiiov = new T4CTTIiov(meg, rxh, rxd);
                    // t4cttiiov.init();
                    // t4cttiiov.unmarshalV10();
                    // if (oracleStatement.returnParamAccessors == null && !t4cttiiov.isIOVectorEmpty()) {
                    // byte abyte0[] = t4cttiiov.getIOVector();
                    // outBindAccessors = t4cttiiov.processRXD(outBindAccessors, numberOfBindPositions, bindBytes,
                    // bindChars, bindIndicators, bindIndicatorSubRange, conversion, tmpBindsByteArray, abyte0,
                    // parameterStream, parameterDatum, parameterOtype, oracleStatement, null, null, null);
                    // }
                    // flag3 = true;
                    break;
                case 16:// _L7
                    // dcb.init(oracleStatement, 0);
                    // definesAccessors = dcb.receive(definesAccessors);
                    // numberOfDefinePositions = dcb.numuds;
                    // definesLength = numberOfDefinePositions;
                    // rxd.setNumberOfColumns(numberOfDefinePositions);
                    break;
                case 19:// _L8
                    // fob.marshal();
                    break;
                case 21:// _L9
                    // int i = meg.unmarshalUB2();
                    // rxd.unmarshalBVC(i);
                    break;
                default:
                    throw new RuntimeException("protocol error");
            }

            // meg.sentCancel = false;
            // meg.pipeState = -1;

        }
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return T4CPacketBuffer.class;
    }

}
