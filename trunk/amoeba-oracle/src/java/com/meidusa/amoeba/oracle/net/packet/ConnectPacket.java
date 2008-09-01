package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-11 下午04:18:18
 */
public class ConnectPacket extends AbstractPacket {

    public static final byte[] CONSTANT_CONNECT_BYTES = new byte[] { 1, 52, 1, 44, 0, 0 };
    protected byte[]           arrayFlag              = new byte[6];
    public int                 sduSize                = NSPDFSDULN;
    public int                 tduSize                = NSPMXSDULN;
    public boolean             anoEnabled             = false;
    public String              data;

    public ConnectPacket(){
        super(NS_PACKT_TYPE_CONNECT);
    }

    @Override
    public void init(byte[] buffer, Connection conn) {
        super.init(buffer, conn);
        sduSize = buffer[14] & 0xff;
        sduSize <<= 8;
        sduSize |= buffer[15] & 0xff;
        tduSize = buffer[16] & 0xff;
        tduSize <<= 8;
        tduSize |= buffer[17] & 0xff;
        dataLen = buffer[24] & 0xff;
        dataLen <<= 8;
        dataLen |= buffer[25] & 0xff;
        if (buffer[32] == NSINADISABLEFORCONNECTION && buffer[33] == NSINADISABLEFORCONNECTION) {
            anoEnabled = false;
        } else {
            anoEnabled = true;
        }
        dataOffset = buffer[27];
        data = extractData();
        /*
         * if (dataLen > 0) { byte[] dataBytes = new byte[dataLen]; System.arraycopy(buffer, dataOffset, data, 0,
         * dataLen); data = new String(dataBytes); }
         */

    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        OracleAbstractPacketBuffer buffer = (OracleAbstractPacketBuffer) absbuffer;
        dataOffset = 34;
        buffer.writeBytes(CONSTANT_CONNECT_BYTES);
        buffer.writeUB2(sduSize);// postion = 14;
        buffer.writeUB2(tduSize);
        buffer.writeUB1((byte) 79);
        buffer.writeUB1((byte) -104);
        buffer.setPosition(22);
        buffer.writeUB1((byte) 0);
        buffer.writeUB1((byte) 1);
        byte[] dataBytes = null;
        if (data != null) {
            dataBytes = data.getBytes();
            buffer.writeUB2(dataBytes.length);
            buffer.setPosition(26);
            buffer.writeUB2(dataOffset);// 写入data 在buffer中偏移位置

        } else {
            buffer.writeUB2((byte) 0);
        }

        buffer.setPosition(32);
        if (!anoEnabled) {
            buffer.writeByte(NSINADISABLEFORCONNECTION);
            buffer.writeByte(NSINADISABLEFORCONNECTION);
        } else {
            buffer.writeByte((byte) 1);
            buffer.writeByte((byte) 1);
        }
        if (dataBytes != null) {
            buffer.setPosition(dataOffset);
            buffer.writeBytes(dataBytes);// 写入data 数据
        }

    }

}
