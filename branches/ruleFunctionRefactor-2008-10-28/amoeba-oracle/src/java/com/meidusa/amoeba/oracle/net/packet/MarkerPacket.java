package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-10-8 ÉÏÎç11:26:20
 */
public class MarkerPacket extends AbstractPacket {

    protected byte  type = 1;
    protected byte  data = 2;
    private boolean isReset;
    private boolean isBreak;

    public MarkerPacket(){
        super(NS_PACKT_TYPE_MARKER);
    }

    public boolean isReset() {
        return isReset;
    }

    public boolean isBreak() {
        return isBreak;
    }

    @Override
    public void init(byte[] buffer, Connection conn) {
        super.init(buffer, conn);
        type = buffer[8];
        switch (type) {
            case 0:
                data = 0;
                isBreak = true;
                break;
            case 1:
                data = buffer[10];
                if (data == 2) {
                    isReset = true;
                } else {
                    isBreak = true;
                }
                break;
            default:
                throw new RuntimeException("Unexpected packet");
        }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        OracleAbstractPacketBuffer buffer = (OracleAbstractPacketBuffer) absbuffer;
        buffer.writeByte(type);
        buffer.writeByte((byte) 0);
        buffer.writeByte(data);
    }

    public static boolean isMarkerType(byte[] buffer) {
        if (buffer != null && buffer.length == MARKER_PACKET_SIZE) {
            return (buffer[4] & 0xff) == NS_PACKT_TYPE_MARKER;
        } else {
            return false;
        }
    }
}
