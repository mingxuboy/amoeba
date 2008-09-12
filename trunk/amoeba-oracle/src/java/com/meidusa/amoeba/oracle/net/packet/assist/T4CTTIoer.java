package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

public class T4CTTIoer {

    final int     MAXERRBUF = 512;

    public short  endToEndECIDSequenceNumber;
    public long   curRowNumber;
    public int    retCode;
    public int    arrayElemWError;
    public int    arrayElemErrno;
    public int    currCursorID;
    public short  errorPosition;
    public short  sqlType;
    public byte   oerFatal;
    public short  flags;
    public short  userCursorOpt;
    public short  upiParam;
    public short  warningFlag;
    public int    osError;
    public short  stmtNumber;
    public short  callNumber;
    public int    pad1;
    public long   successIters;
    public int    partitionId;
    public short  tableId;
    public int    slotNumber;
    public long   rba;
    public long   blockNumber;
    public int    warnFlag;
    public String errorMsg;

    public T4CTTIoer(){
        warnFlag = 0;
    }

    public void init() {
        retCode = 0;
        errorMsg = null;
    }

    public boolean isErrorPacket() {
        return retCode != 0;
    }

    public int unmarshal(T4CPacketBuffer meg) {
        if (meg.versionNumber >= 10000) {
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
            byte[] msg = meg.unmarshalCLRforREFS();
            errorMsg = meg.getConversion().CharBytesToString(msg, msg.length);
        }
        return currCursorID;
    }

    public void unmarshalWarning(T4CPacketBuffer meg) {
        retCode = meg.unmarshalUB2();
        int warnLength = meg.unmarshalUB2();
        warnFlag = meg.unmarshalUB2();
        if (retCode != 0 && warnLength > 0) {
            errorMsg = new String(meg.unmarshalCHR(warnLength));
        }
    }

    public void marshal(T4CPacketBuffer meg) {
        if (meg.versionNumber >= 10000) {
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
            meg.marshalDALC(errorMsg.getBytes());
        }
    }
}
