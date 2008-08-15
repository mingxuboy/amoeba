package com.meidusa.amoeba.oracle.packet;

import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * Oracle,Ano格式的数据包buffer解析
 * 
 * @author hexianmao
 * @version 2008-8-7 下午05:17:56
 */
public class AnoPacketBuffer extends AbstractPacketBuffer implements OraclePacketConstant {

    public AnoPacketBuffer(byte[] buf){
        super(buf);
    }

    public AnoPacketBuffer(int size){
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

    public short receiveUB1() {
        readDataLength(2);
        return readUB1();
    }

    public void sendUB1(short s) {
        sendPktHeader(1, 2);
        writeUB1(s);
    }

    public int receiveUB2() {
        readDataLength(3);
        return readUB2();
    }

    public void sendUB2(int i) {
        sendPktHeader(2, 3);
        writeUB2(i);
    }

    public long receiveUB4() {
        readDataLength(4);
        return readUB4();
    }

    public void sendUB4(long l) {
        sendPktHeader(4, 4);
        writeUB4(l);
    }

    public byte[] receiveRaw() {
        int l = readDataLength(1);
        byte[] b = new byte[l];
        readBytes(b, 0, b.length);
        return b;
    }

    public void sendRaw(byte[] ab) {
        sendPktHeader(ab.length, 1);
        writeBytes(ab);
    }

    public int[] receiveUB2Array() {
        readDataLength(1);
        long l = readUB4();
        int i = readUB2();
        long l1 = readUB4();
        int[] ai = new int[(int) l1];
        if (l != AnoServices.NA_MAGIC || i != 3) {
            throw new RuntimeException("Error in array header received");
        }
        for (int j = 0; j < ai.length; j++) {
            ai[j] = readUB2();
        }
        return ai;
    }

    public void sendUB2Array(int[] ai) {
        sendPktHeader(ai.length * 2 + 10, 1);
        writeUB4(AnoServices.NA_MAGIC);
        writeUB2(3);
        writeUB4(ai.length);
        for (int i = 0; i < ai.length; i++) {
            writeUB2(ai[i]);
        }
    }

    public String receiveString() {
        int l = readDataLength(0);
        byte[] ab = new byte[l];
        readBytes(ab, 0, ab.length);
        return new String(ab);
    }

    public void sendString(String s) {
        byte[] ab = s.getBytes();
        sendPktHeader(ab.length, 0);
        writeBytes(ab);
    }

    // //////////////////////////////////////////////////////
    // 含有逻辑的封装
    public long receiveVersion() {
        readDataLength(5);
        return readUB4();
    }

    public void writeVersion() {
        writeUB4(VERSION);
    }

    public void sendVersion() {
        sendPktHeader(4, 5);
        writeVersion();
    }

    public int receiveStatus() {
        readDataLength(6);
        return readUB2();
    }

    public void sendStatus(int i) {
        sendPktHeader(2, 6);
        writeUB2(i);
    }

    // //////////////////////////////////////////////////////
    // 私有函数
    private void sendPktHeader(int k, int l) {
        check(k, l);
        writeUB2(k);
        writeUB2(l);
    }

    private int readDataLength(int k) {
        int l = readUB2();
        int i = readUB2();
        check(l, i, k);
        return l;
    }

    private void check(int k, int l, int i) {
        if (l != i) {
            throw new RuntimeException("Unexpected NA Packet Type received");
        } else {
            check(k, l);
        }
    }

    private void check(int k, int l) {
        if (l < 0 || l > 7) throw new RuntimeException("Invalid NA packet type received");
        switch (l) {
            case 0:
            case 1:
                break;
            case 2:
                if (k > 1) throw new RuntimeException("Invalid length for an NA type");
                break;
            case 3:
            case 6:
                if (k > 2) throw new RuntimeException("Invalid length for an NA type");
                break;
            case 4:
            case 5:
                if (k > 4) throw new RuntimeException("Invalid length for an NA type");
                break;
            case 7:
                if (k < 10) throw new RuntimeException("Invalid length for an NA type");
                break;
            default:
                throw new RuntimeException("Invalid NA packet type received");
        }
    }

}
