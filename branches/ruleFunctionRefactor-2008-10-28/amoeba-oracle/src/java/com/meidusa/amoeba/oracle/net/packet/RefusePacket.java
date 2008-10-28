package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.Connection;

/**
 * 
 * @author struct
 *
 */
public class RefusePacket extends AbstractPacket {
	protected int userReason;
    protected int systemReason;
	public RefusePacket() {
		super(SQLnetDef.NS_PACKT_TYPE_REFUTE);
	}

	public void init(byte[] buffer, Connection conn) {
        super.init(buffer, conn);
        userReason = buffer[8];
        systemReason = buffer[9];
        this.dataOffset = 12;
        dataLen = buffer[10] & 0xff;
        dataLen <<= 8;
        dataLen |= buffer[11] & 0xff;
        extractData();
	}
}
