package com.meidusa.amoeba.oracle.net.packet;


public class T4CTTIoer {

    final int               MAXERRBUF = 512;

    private T4CPacketBuffer meg;
    short                   endToEndECIDSequenceNumber;
    long                    curRowNumber;
    int                     retCode;
    int                     arrayElemWError;
    int                     arrayElemErrno;
    int                     currCursorID;
    short                   errorPosition;
    short                   sqlType;
    byte                    oerFatal;
    short                   flags;
    short                   userCursorOpt;
    short                   upiParam;
    short                   warningFlag;
    int                     osError;
    short                   stmtNumber;
    short                   callNumber;
    int                     pad1;
    long                    successIters;
    int                     partitionId;
    short                   tableId;
    int                     slotNumber;
    long                    rba;
    long                    blockNumber;
    int                     warnLength;
    int                     warnFlag;
    int[]                   errorLength;
    byte[]                  errorMsg;

    public T4CTTIoer(T4CPacketBuffer buffer){
        this.meg = buffer;
        warnLength = 0;
        warnFlag = 0;
        errorLength = new int[1];
    }

    public void init() {
        retCode = 0;
        errorMsg = null;
    }

    public boolean isErrorPacket() {
        return retCode != 0;
    }

    public int unmarshal() {
        if (T4CPacketBuffer.versionNumber >= 10000) {
            endToEndECIDSequenceNumber = (short) meg.unmarshalUB2();
            // connection.endToEndECIDSequenceNumber = endToEndECIDSequenceNumber;
        }
        curRowNumber = meg.unmarshalUB4();
        retCode = meg.unmarshalUB2();
        arrayElemWError = meg.unmarshalUB2();
        arrayElemErrno = meg.unmarshalUB2();
        currCursorID = meg.unmarshalUB2();
        errorPosition = meg.unmarshalSB2();
        sqlType = meg.unmarshalUB1();
        oerFatal = meg.unmarshalSB1();
        flags = meg.unmarshalSB2();
        userCursorOpt = meg.unmarshalSB2();
        upiParam = meg.unmarshalUB1();
        warningFlag = meg.unmarshalUB1();
        rba = meg.unmarshalUB4();
        partitionId = meg.unmarshalUB2();
        tableId = meg.unmarshalUB1();
        blockNumber = meg.unmarshalUB4();
        slotNumber = meg.unmarshalUB2();
        osError = meg.unmarshalSWORD();
        stmtNumber = meg.unmarshalUB1();
        callNumber = meg.unmarshalUB1();
        pad1 = meg.unmarshalUB2();
        successIters = meg.unmarshalUB4();
        if (retCode != 0) {
            errorMsg = meg.unmarshalCLRforREFS();
            errorLength[0] = errorMsg.length;
        }
        return currCursorID;
    }

    void unmarshalWarning() {
        retCode = meg.unmarshalUB2();
        warnLength = meg.unmarshalUB2();
        warnFlag = meg.unmarshalUB2();
        if (retCode != 0 && warnLength > 0) {
            errorMsg = meg.unmarshalCHR(warnLength);
            errorLength[0] = warnLength;
        }
    }

    public void marshal(T4CPacketBuffer meg) {
        if (T4CPacketBuffer.versionNumber >= 10000) {
            meg.marshalUB2(endToEndECIDSequenceNumber);
        }
        meg.marshalUB4(curRowNumber);
        meg.marshalUB2(retCode);
        meg.marshalUB2(arrayElemWError);
        meg.marshalUB2(arrayElemErrno);
        meg.marshalUB2(currCursorID);
        meg.marshalUB2(errorPosition);
        meg.marshalUB1(sqlType);
        meg.marshalSB1(oerFatal);
        meg.marshalSB2(flags);
        meg.marshalSB2(userCursorOpt);
        meg.marshalUB1(upiParam);
        meg.marshalUB1(warningFlag);
        meg.marshalUB4(rba);
        meg.marshalUB2(partitionId);
        meg.marshalUB1(tableId);
        meg.marshalUB4(blockNumber);
        meg.marshalUB2(slotNumber);
        meg.marshalSWORD(osError);
        meg.marshalUB1(stmtNumber);
        meg.marshalUB1(callNumber);
        meg.marshalUB2(pad1);
        meg.marshalUB4(successIters);
        if (retCode != 0) {
            meg.marshalDALC(errorMsg);
        }
    }
}
