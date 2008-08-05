package com.meidusa.amoeba.oracle.packet;

public class AcceptPacket extends AbstractPacket {
	protected int version;
    protected int options;
    protected int sduSize;
    protected int tduSize;
    protected int myHWByteOrder;
    protected int flag0;
    protected int flag1;
    
	public void init(byte[] buffer) {
		super.init(buffer);
		version = buffer[8] & 0xff;
        version <<= 8;
        version |= buffer[9] & 0xff;
        options = buffer[10] & 0xff;
        options <<= 8;
        options |= buffer[11] & 0xff;
        sduSize = buffer[12] & 0xff;
        sduSize <<= 8;
        sduSize |= buffer[13] & 0xff;
        tduSize = buffer[14] & 0xff;
        tduSize <<= 8;
        tduSize |= buffer[15] & 0xff;
        myHWByteOrder = buffer[16] & 0xff;
        myHWByteOrder <<= 8;
        myHWByteOrder |= buffer[17] & 0xff;
        dataLen = buffer[18] & 0xff;
        dataLen <<= 8;
        dataLen |= buffer[19] & 0xff;
        dataOff = buffer[20] & 0xff;
        dataOff <<= 8;
        dataOff |= buffer[21] & 0xff;
        flag0 = buffer[22];
        flag1 = buffer[23];
	}
}
