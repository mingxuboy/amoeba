package com.meidusa.amoeba.oracle.net.packet;

public class T4CTTIoer {
	private T4CPacketBuffer meg;
	final int                   MAXERRBUF                                   = 512;
    long                        curRowNumber;
    int                         retCode;
    int                         arrayElemWError;
    int                         arrayElemErrno;
    int                         currCursorID;
    short                       errorPosition;
    short                       sqlType;
    byte                        oerFatal;
    short                       flags;
    short                       userCursorOpt;
    short                       upiParam;
    short                       warningFlag;
    int                         osError;
    short                       stmtNumber;
    short                       callNumber;
    int                         pad1;
    long                        successIters;
    int                         partitionId;
    int                         tableId;
    int                         slotNumber;
    long                        rba;
    long                        blockNumber;
    int                         warnLength;
    int                         warnFlag;
    int                         errorLength[];
    byte                        errorMsg[];
	public T4CTTIoer(T4CPacketBuffer buffer){
		this.meg = buffer;
		warnLength = 0;
        warnFlag = 0;
        errorLength = new int[1];
	}
	
	public void init(){
		retCode = 0;
        errorMsg = null;
	}
	
	public boolean isErrorPacket(){
		return retCode != 0; 
	}
	
	public int unmarshal(){
		if (T4CPacketBuffer.versionNumber >= 10000) {
            short word0 = (short) meg.unmarshalUB2();
            //connection.endToEndECIDSequenceNumber = word0;
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
        	//TODO
            //errorMsg = meg.unmarshalCLRforREFS();
            errorLength[0] = errorMsg.length;
        }
        return currCursorID;
	}
}
