package com.meidusa.amoeba.oracle.packet;

import com.meidusa.amoeba.oracle.io.OraclePacketConstant;

/**
 * Ano数据包，buffer的读写和解析。
 * 
 * @author hexianmao
 * @version 2008-8-7 下午05:17:56
 */
public class AnoPacketBuffer {

    private int    length   = 0;

    private int    position = 0;

    private byte[] buffer   = null;

    public AnoPacketBuffer(byte[] buf){
        this.buffer = new byte[buf.length + 1];
        System.arraycopy(buf, 0, buffer, 0, buf.length);
        this.length = buf.length;
        this.position = OraclePacketConstant.HEADER_SIZE;
    }

    protected short readUB1() {
        return (short) buffer[this.position++];
    }

    protected int readUB2() {
        byte[] abyte0 = new byte[2];
        abyte0[0] = buffer[this.position++];
        abyte0[1] = buffer[this.position++];
        int k = (int) byteArray2Long(abyte0);
        return k & 0xffff;
    }

    protected long readUB4() {
        byte[] abyte0 = new byte[4];
        abyte0[0] = buffer[this.position++];
        abyte0[1] = buffer[this.position++];
        abyte0[2] = buffer[this.position++];
        abyte0[3] = buffer[this.position++];
        return byteArray2Long(abyte0);
    }

    protected byte[] receiveRaw() {
        int k = readDataLength(1);
        byte[] b = new byte[k];
        for (int i = 0; i < k; i++) {
            b[i] = buffer[this.position++];
        }
        return b;
    }

    protected long receiveVersion() {
        readDataLength(5);
        return readUB4();
    }

    protected int receiveUB2() {
        readDataLength(3);
        int k = readUB2();
        return k & 0xffff;
    }

    protected short receiveUB1() {
        readDataLength(2);
        return readUB1();
    }

    protected long receiveUB4() {
        readDataLength(4);
        return readUB4();
    }

    protected int[] receiveUB2Array() {
        readDataLength(1);
        long l = readUB4();
        int i1 = readUB2();
        long l1 = readUB4();
        int[] ai = new int[(int) l1];
        if (l != 0xffffffffdeadbeefL || i1 != 3) {
            throw new RuntimeException("Error in array header received");
        }

        for (int j1 = 0; j1 < ai.length; j1++) {
            ai[j1] = readUB2();
        }

        return ai;
    }

    protected String receiveString() {
        int k = readDataLength(0);
        byte[] b = new byte[k];
        for (int i = 0; i < k; i++) {
            b[i] = buffer[this.position++];
        }
        return new String(b);
    }

    protected int receiveStatus() {
        readDataLength(6);
        return readUB2();
    }

    protected long getVersion() {
        return 0x8105000L;
    }

    protected void sendRaw(byte[] abyte0) {
        sendPktHeader(abyte0.length, 1);
        writeArray(abyte0);
    }

    protected void sendUB2Array(int[] ai) {
        sendPktHeader(10 + ai.length * 2, 1);
        writeUB4(0xffffffffdeadbeefL);
        writeUB2(3);
        writeUB4(ai.length);
        for (int k = 0; k < ai.length; k++) {
            writeUB2(ai[k] & 0xffff);
        }
    }

    protected void writeVersion() {
        writeUB4(getVersion());
    }

    protected void sendVersion() {
        sendPktHeader(4, 5);
        writeUB4(getVersion());
    }

    protected void sendUB1(short word0) {
        sendPktHeader(1, 2);
        writeUB1(word0);
    }

    protected void sendUB2(int k) {
        sendPktHeader(2, 3);
        writeUB2(k);
    }

    protected void sendString(String s) {
        byte[] b = s.getBytes();
        sendPktHeader(b.length, 0);
        writeArray(b);
    }

    protected void sendStatus(int k) {
        sendPktHeader(2, 6);
        writeUB2(k);
    }

    protected void sendUB4(long l) {
        sendPktHeader(4, 4);
        writeUB4(l);
    }

    protected void writeUB1(short word0) {
        ensureCapacity(1);
        byte[] abyte0 = new byte[1];
        abyte0[0] = buffer[this.position++];
        int2ByteArray(0xff & word0, abyte0);
    }

    protected void writeUB2(int k) {
        ensureCapacity(2);
        byte abyte0[] = new byte[2];
        abyte0[0] = buffer[this.position++];
        abyte0[1] = buffer[this.position++];
        int2ByteArray((short) (0xffff & k), abyte0);
    }

    protected void writeUB4(long l) {
        ensureCapacity(4);
        byte abyte0[] = new byte[4];
        abyte0[0] = buffer[this.position++];
        abyte0[1] = buffer[this.position++];
        abyte0[0] = buffer[this.position++];
        abyte0[1] = buffer[this.position++];
        int2ByteArray((int) (-1L & l), abyte0);
    }

    protected void writeArray(byte[] b) {
        ensureCapacity(b.length);
        System.arraycopy(b, 0, buffer, position, b.length);
        this.position += b.length;
    }

    protected void sendPktHeader(int k, int l) {
        check(k, l);
        writeUB2(k);
        writeUB2(l);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPosition() {
        return position;
    }

    private int readDataLength(int k) {
        int l = readUB2();
        int i1 = readUB2();
        check(l, i1, k);
        return l;
    }

    private void check(int k, int l, int i1) {
        if (l != i1) {
            throw new RuntimeException();
        } else {
            check(k, l);
        }
    }

    private void check(int k, int l) {
        if (l < 0 || l > 7)
            throw new RuntimeException();// TODO replace this Exception
        switch (l) {
            case 0:
            case 1:
                break;
            case 2:
                if (k > 1)
                    throw new RuntimeException();
                break;
            case 3:
            case 6:
                if (k > 2)
                    throw new RuntimeException();
                break;
            case 4:
            case 5:
                if (k > 4)
                    throw new RuntimeException();
                break;
            case 7:
                if (k < 10)
                    throw new RuntimeException();
                break;
            default:
                throw new RuntimeException();
        }
    }

    /**
     * <pre>
     * 将int k存储到abyte0数组中，高位存在abyte0数组的前面。
     * 比如k：0xdeadbeef
     * 则abyte0内容如下：
     * abyte0[0]:0xde
     * abyte0[1]:0xad
     * abyte0[2]:0xbe
     * abyte0[3]:0xef
     * </pre>
     */
    private byte int2ByteArray(int k, byte[] abyte0) {
        byte byte0 = 0;
        for (int l = abyte0.length - 1; l >= 0; l--) {
            abyte0[byte0++] = (byte) ((k >>> (8 * l)) & 0xff);
        }
        return byte0;
    }

    /**
     * <pre>
     * 将abyte0数组值转换成long数值，
     * 先读取的作为long的高位，依次往下移位。
     * 比如abyte0:
     * abyte0[0]:0xde
     * abyte0[1]:0xad
     * abyte0[2]:0xbe
     * abyte0[3]:0xef
     * 转换后的值：0xdeadbeefL
     * </pre>
     */
    private long byteArray2Long(byte[] abyte0) {
        long l = 0L;
        for (int k = 0; k < abyte0.length; k++) {
            l |= (abyte0[k] & 0xff) << (8 * (abyte0.length - 1 - k));
        }

        l &= -1L;
        return l;
    }

    /**
     * 增加buffer长度
     */
    private void ensureCapacity(int len) {
        if ((position + len) > getLength()) {
            if ((position + len) < buffer.length) {
                setLength(buffer.length);
            } else {
                int newLength = (int) (buffer.length * 1.25);

                if (newLength < (buffer.length + len)) {
                    newLength = buffer.length + (int) (len * 1.25);
                }

                byte[] newBytes = new byte[newLength];
                System.arraycopy(this.buffer, 0, newBytes, 0, buffer.length);
                buffer = newBytes;
                setLength(buffer.length);
            }
        }
    }

}
