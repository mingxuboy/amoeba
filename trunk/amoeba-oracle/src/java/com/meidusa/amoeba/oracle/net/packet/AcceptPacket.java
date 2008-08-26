package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-11 ÏÂÎç04:17:38
 */
public class AcceptPacket extends AbstractPacket {

    protected int  version       = 308;
    protected int  options       = 0;
    protected int  sduSize       = NSPDFSDULN;
    protected int  tduSize       = NSPMXSDULN;
    protected int  myHWByteOrder = 256;
    protected byte flag0         = 69;
    protected byte flag1         = 0;

    public AcceptPacket(){
        super(NS_PACKT_TYPE_ACCEPT);
    }

    @Override
    public void init(byte[] buffer, Connection conn) {
        super.init(buffer, conn);
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
        dataOffset = buffer[20] & 0xff;
        dataOffset <<= 8;
        dataOffset |= buffer[21] & 0xff;
        flag0 = buffer[22];
        flag1 = buffer[23];
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        OracleAbstractPacketBuffer buffer = (OracleAbstractPacketBuffer) absbuffer;
        buffer.writeUB2(version);
        buffer.writeUB2(options);
        buffer.writeUB2(sduSize);
        buffer.writeUB2(tduSize);
        buffer.writeUB2(myHWByteOrder);
        buffer.writeUB2(0);
        buffer.writeUB2(dataOffset);
        buffer.writeUB1(flag0);
        buffer.writeUB1(flag1);
    }

}
