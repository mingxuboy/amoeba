package com.meidusa.amoeba.oracle.packet;

import java.io.IOException;

import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * Oracle,T4C格式的数据包buffer解析
 * 
 * @author hexianmao
 * @version 2008-8-13 上午01:02:40
 */
public class T4CPacketBuffer extends AbstractPacketBuffer implements OraclePacketConstant {

    final byte[] tmpBuffer1 = new byte[1];
    final byte[] tmpBuffer2 = new byte[2];
    final byte[] tmpBuffer3 = new byte[3];
    final byte[] tmpBuffer4 = new byte[4];
    final byte[] tmpBuffer5 = new byte[5];
    final byte[] tmpBuffer6 = new byte[6];
    final byte[] tmpBuffer7 = new byte[7];
    final byte[] tmpBuffer8 = new byte[8];

    T4CTypeRep   types;

    public T4CPacketBuffer(byte[] buf){
        super(buf);
        initType();
    }

    public T4CPacketBuffer(int size){
        super(size);
        initType();
    }

    /**
     * 发送有符号byte
     */
    void marshalSB1(byte b) {
        ensureCapacity(1);
        buffer[position++] = b;
    }

    /**
     * 发送无符号byte
     */
    void marshalUB1(short s) {
        marshalSB1((byte) (s & 0xff));
    }

    void marshalSB2(short s) {
        byte b = int2Buffer(s, tmpBuffer2, (byte) 1);
        if (b != 0) {
            ensureCapacity(b);
            System.arraycopy(tmpBuffer2, 0, buffer, position, b);
            position += b;
        }
    }

    void marshalUB2(int i) {
        marshalSB2((short) (i & 0xffff));
    }

    void marshalSB4(int i) {
        byte b = int2Buffer(i, tmpBuffer4, (byte) 2);
        if (b != 0) {
            ensureCapacity(b);
            System.arraycopy(tmpBuffer4, 0, buffer, position, b);
            position += b;
        }
    }

    void marshalUB4(long l) {
        marshalSB4((int) (l & -1L));
    }

    void marshalSB8(long l) {
        byte b = long2Buffer(l, tmpBuffer8, (byte) 3);
        if (b != 0) {
            ensureCapacity(b);
            System.arraycopy(tmpBuffer8, 0, buffer, position, b);
            position += b;
        }
    }

    void marshalSWORD(int i) {
        marshalSB4(i);
    }

    void marshalUWORD(long l) {
        marshalSB4((int) (l & -1L));
    }

    void marshalB1Array(byte[] ab) {
    }

    void marshalB1Array(byte[] ab, int i, int j) {
    }

    void marshalUB4Array(long[] al) {
    }

    void marshalO2U(boolean flag) {
    }

    void marshalNULLPTR() {
    }

    void marshalPTR() {
    }

    void marshalCHR(byte[] ab) {
    }

    void marshalCHR(byte[] ab, int i, int j) {
    }

    void marshalCLR(byte[] ab, int i, int j) {
    }

    void marshalCLR(byte[] ab, int i) {
    }

    void marshalKEYVAL(byte[][] ab, int[] ai, byte[][] ab1, int[] ai1, byte[] ab2, int i) {
    }

    void marshalKEYVAL(byte[][] ab, byte[][] ab1, byte[] ab2, int i) {
    }

    void marshalDALC(byte[] ab) {
    }

    /**
     * 读取有符号byte
     */
    byte unmarshalSB1() {
        return buffer[position++];
    }

    /**
     * 读取无符号byte
     */
    short unmarshalUB1() {
        return (short) (buffer[position++] & 0xff);
    }

    short unmarshalSB2() {
        return (short) unmarshalUB2();
    }

    int unmarshalUB2() {
        int i = (int) buffer2Value((byte) 1);
        return i & 0xffff;
    }

    int unmarshalSB4() {
        return 0;
    }

    long unmarshalUB4() {
        return 0;
    }

    long unmarshalSB8() {
        return 0;
    }

    int unmarshalSWORD() {
        return 0;
    }

    long unmarshalUWORD() {
        return 0;
    }

    byte[] unmarshalCHR(int i) {
        return null;
    }

    void unmarshalCLR(byte[] ab, int i, int[] ai) {
    }

    byte[] unmarshalCLR(int i, int[] ai) {
        return null;
    }

    void unmarshalCLR(byte[] ab, int i, int[] ai, int j) {
    }

    int unmarshalKEYVAL(byte[][] ab0, byte[][] ab1, int i) {
        return 0;
    }

    long unmarshalDALC(byte[] ab, int i, int[] ai) {
        return 0;
    }

    byte[] unmarshalDALC() {
        return null;
    }

    byte[] unmarshalDALC(int[] ai) {
        return null;
    }

    // ////////////////////////////////////////////////////////
    void addPtr(byte b) {
    }

    private long buffer2Value(byte b) {
        int i = 0;
        boolean flag = false;
        if ((types.rep[b] & 1) > 0) {
            i = buffer[position++] & 0xff;
            if ((i & 0x80) > 0) {
                i &= 0x7f;
                flag = true;
            }
            if (i < 0) throw new RuntimeException("无法从套接字读取更多的数据");
            if (i == 0) return 0L;
            if ((b == 1 && i > 2) || (b == 2 && i > 4) || (b == 3 && i > 8)) {
                throw new RuntimeException("类型长度大于最大值");
            }
        } else if (b == 1) {
            i = 2;
        } else if (b == 2) {
            i = 4;
        } else if (b == 3) {
            i = 8;
        }
        byte[] ab = getTmpBuffer(i);
        System.arraycopy(buffer, position, ab, 0, i);
        position += i;

        long l1 = 0L;
        for (int j = 0; j < ab.length; j++) {
            long l = 0L;
            if ((types.rep[b] & 2) > 0) {
                l = (long) (ab[ab.length - 1 - j] & 0xff) & 255L;
            } else {
                l = (long) (ab[j] & 0xff) & 255L;
            }
            l1 |= l << (8 * (ab.length - 1 - j));
        }
        if (b != 3) {
            l1 &= -1L;
        }
        if (flag) l1 = -l1;
        return l1;
    }

    long buffer2Value(byte b, byte[] buffer) throws IOException {
        int i = 0;
        boolean flag = false;
        if ((types.rep[b] & 1) > 0) {
            i = buffer[0] & 0xff;
            if ((i & 0x80) > 0) {
                i &= 0x7f;
                flag = true;
            }
            if (i < 0) throw new RuntimeException("无法从套接字读取更多的数据");
            if (i == 0) return 0L;
            if ((b == 1 && i > 2) || (b == 2 && i > 4)) {
                throw new RuntimeException("类型长度大于最大值");
            }
        } else if (b == 1) {
            i = 2;
        } else if (b == 2) {
            i = 4;
        }
        byte[] ab = new byte[i];
        System.arraycopy(buffer, 1, ab, 0, i);

        long l = 0L;
        for (int j = 0; j < ab.length; j++) {
            short s = 0;
            if ((types.rep[b] & 2) > 0) {
                s = (short) (ab[ab.length - 1 - j] & 0xff);
            } else {
                s = (short) (ab[j] & 0xff);
            }
            l |= s << (8 * (ab.length - 1 - j));
        }
        l &= -1L;
        if (flag) l = -l;
        return l;
    }

    private byte[] getTmpBuffer(int i) {
        switch (i) {
            case 1:
                return tmpBuffer1;
            case 2:
                return tmpBuffer2;
            case 3:
                return tmpBuffer3;
            case 4:
                return tmpBuffer4;
            case 5:
                return tmpBuffer5;
            case 6:
                return tmpBuffer6;
            case 7:
                return tmpBuffer7;
            case 8:
                return tmpBuffer8;
            default:
                return new byte[i];
        }
    }

    private byte int2Buffer(int i, byte[] ab, byte b) {
        boolean flag = true;
        byte b1 = 0;
        for (int j = ab.length - 1; j >= 0; j--) {
            ab[b1] = (byte) ((i >>> (8 * j)) & 0xff);
            if ((types.rep[b] & 1) > 0) {
                if (!flag || ab[b1] != 0) {
                    flag = false;
                    b1++;
                }
            } else {
                b1++;
            }
        }

        if ((types.rep[b] & 1) > 0) {
            writeByte(b1);
        }
        if ((types.rep[b] & 2) > 0) {
            reverseArray(ab, b1);
        }
        return b1;
    }

    byte long2Buffer(long l, byte[] ab, byte b) {
        boolean flag = true;
        byte b1 = 0;
        for (int i = ab.length - 1; i >= 0; i--) {
            ab[b1] = (byte) ((l >>> (8 * i)) & 255L);
            if ((types.rep[b] & 1) > 0) {
                if (!flag || ab[b1] != 0) {
                    flag = false;
                    b1++;
                }
            } else {
                b1++;
            }
        }

        if ((types.rep[b] & 1) > 0) {
            writeByte(b1);
        }
        if ((types.rep[b] & 2) > 0) {
            reverseArray(ab, b1);
        }
        return b1;
    }

    private void reverseArray(byte[] ab, byte b) {
        for (int i = 0; i < b / 2; i++) {
            byte b1 = ab[i];
            ab[i] = ab[b - 1 - i];
            ab[b - 1 - i] = b1;
        }
    }

    private void initType() {
        types = new T4CTypeRep();
        types.setRep((byte) 1, (byte) 2);
    }

}
