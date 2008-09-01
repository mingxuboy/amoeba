package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.util.DBConversion;
import com.meidusa.amoeba.oracle.util.T4CTypeRep;

public class OracleAbstractPacketBuffer extends AbstractPacketBuffer {

    public short               versionNumber;

    protected OracleConnection oconn;

    public OracleAbstractPacketBuffer(byte[] buf){
        super(buf);
    }

    public OracleAbstractPacketBuffer(int size){
        super(size);
    }

    public void init(Connection conn) {
        super.init(conn);
        this.oconn = (OracleConnection) conn;
    }

    public short readUB1() {
        return (short) (buffer[position++] & 0xff);
    }

    public void writeUB1(short s) {
        writeByte((byte) (s & 0xff));
    }

    public int readUB2() {
        return ((buffer[position++] & 0xff) << 8) | (buffer[position++] & 0xff);
    }

    public void writeUB2(int i) {
        ensureCapacity(2);
        int j = i & 0xffff;
        buffer[position++] = (byte) ((j >>> 8) & 0xff);
        buffer[position++] = (byte) (j & 0xff);
    }

    public long readUB4() {
        return ((buffer[position++] & 0xffL) << 24) | ((buffer[position++] & 0xff) << 16) | ((buffer[position++] & 0xff) << 8) | (buffer[position++] & 0xff);
    }

    public void writeUB4(long l) {
        ensureCapacity(4);
        long m = l & 0xffffffffL;
        buffer[position++] = (byte) ((m >>> 24) & 0xff);
        buffer[position++] = (byte) ((m >>> 16) & 0xff);
        buffer[position++] = (byte) ((m >>> 8) & 0xff);
        buffer[position++] = (byte) (m & 0xff);
    }

    // ///////////////////////////////////////////////////////////////
    protected T4CTypeRep getTypeRep() {
        return this.oconn.getRep();
    }

    protected DBConversion getConversion() {
        return this.oconn.getConversion();
    }

    protected void setConversion(DBConversion conversion) {
        this.oconn.setConversion(conversion);
    }

}
