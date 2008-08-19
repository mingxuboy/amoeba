package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.net.OracleConnection;
import com.meidusa.amoeba.oracle.util.T4CTypeRep;

public class OracleAbstractPacketBuffer extends AbstractPacketBuffer {
	public static short versionNumber;//用于存放全局服务器端的版本，amoeba 所代理的多个 oracle server 在版本上面必须要一致
	protected T4CTypeRep typeRep;
	
	public OracleAbstractPacketBuffer(byte[] buf) {
		super(buf);
	}

	public OracleAbstractPacketBuffer(int size) {
		super(size);
	}
	
	public short readUB1() {
        return (short) (buffer[position++] & 0xff);
    }

    public void writeUB1(short s) {
        writeByte((byte) (s & 0xff));
    }

    public int readUB2() {
        int i = 0;
        i |= (buffer[position++] & 0xff) << 8;
        i |= (buffer[position++] & 0xff);
        i &= 0xfffff;
        return i;
    }

    public void writeUB2(int i) {
        ensureCapacity(2);
        int j = 0xffff & i;
        buffer[position++] = (byte) ((j >>> 8) & 0xff);
        buffer[position++] = (byte) (j & 0xff);
    }

    public long readUB4() {
        long l = 0L;
        l |= ((buffer[position++] & 0xff) << 24);
        l |= ((buffer[position++] & 0xff) << 16);
        l |= ((buffer[position++] & 0xff) << 8);
        l |= (buffer[position++] & 0xff);
        l &= -1L;
        return l;
    }

    public void writeUB4(long l) {
        ensureCapacity(4);
        long m = l & -1L;
        buffer[position++] = (byte) ((m >>> 24) & 0xff);
        buffer[position++] = (byte) ((m >>> 16) & 0xff);
        buffer[position++] = (byte) ((m >>> 8) & 0xff);
        buffer[position++] = (byte) (m & 0xff);
    }

    public T4CTypeRep getTypeRep() {
		return typeRep;
	}

	public void init(Connection conn){
    	super.init(conn);
    	OracleConnection oconn = (OracleConnection)conn ;
    	this.typeRep = oconn.getRep(); 
    }
}
