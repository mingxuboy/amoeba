package com.meidusa.amoeba.oracle.net.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.meidusa.amoeba.oracle.io.OraclePacketConstant;

/**
 * Oracle,T4C格式的数据包buffer解析
 * 
 * @author hexianmao
 * @version 2008-8-13 上午01:02:40
 */
public class T4CPacketBuffer extends OracleAbstractPacketBuffer implements OraclePacketConstant {

    static final int  TTCC_MXL   = 252;
    static final int  TTCC_ESC   = 253;
    static final int  TTCC_LNG   = 254;
    static final int  TTCC_ERR   = 255;
    static final int  TTCC_MXIN  = 64;
    static final byte TTCLXMULTI = 1;
    static final byte TTCLXMCONV = 2;
    static final int  FREE       = -1;
    static final int  SEND       = 1;
    static final int  RECEIVE    = 2;

    final byte[]      tmpBuffer1 = new byte[1];
    final byte[]      tmpBuffer2 = new byte[2];
    final byte[]      tmpBuffer3 = new byte[3];
    final byte[]      tmpBuffer4 = new byte[4];
    final byte[]      tmpBuffer5 = new byte[5];
    final byte[]      tmpBuffer6 = new byte[6];
    final byte[]      tmpBuffer7 = new byte[7];
    final byte[]      tmpBuffer8 = new byte[8];

    final byte[]      ignored    = new byte[255];
    final int[]       retLen     = new int[1];

    public T4CPacketBuffer(byte[] buf){
        super(buf);
    }

    public T4CPacketBuffer(int size){
        super(size);
    }

    /**
     * 发送有符号byte
     */
    public void marshalSB1(byte b) {
        writeByte(b);
    }

    /**
     * 发送无符号byte
     */
    public void marshalUB1(short s) {
        marshalSB1((byte) (s & 0xff));
    }

    public void marshalSB2(short s) {
        byte b = int2Buffer(s, tmpBuffer2, (byte) 1);
        if (b != 0) {
            writeBytes(tmpBuffer2, 0, b);
        }
    }

    public void marshalUB2(int i) {
        marshalSB2((short) (i & 0xffff));
    }

    public void marshalSB4(int i) {
        byte b = int2Buffer(i, tmpBuffer4, (byte) 2);
        if (b != 0) {
            writeBytes(tmpBuffer4, 0, b);
        }
    }

    public void marshalUB4(long l) {
        marshalSB4((int) (l & -1L));
    }

    public void marshalSB8(long l) {
        byte b = long2Buffer(l, tmpBuffer8, (byte) 3);
        if (b != 0) {
            writeBytes(tmpBuffer8, 0, b);
        }
    }

    public void marshalSWORD(int i) {
        marshalSB4(i);
    }

    public void marshalUWORD(long l) {
        marshalSB4((int) (l & -1L));
    }

    public void marshalB1Array(byte[] ab) {
        if (ab.length > 0) {
            writeBytes(ab, 0, ab.length);
        }
    }

    public void marshalB1Array(byte[] ab, int i, int j) {
        if (ab.length > 0) {
            writeBytes(ab, i, j);
        }
    }

    public void marshalUB4Array(long[] al) {
        for (int i = 0; i < al.length; i++) {
            marshalSB4((int) (al[i] & -1L));
        }
    }

    public void unmarshalUB4Array(long[] al) {
        for (int i = 0; i < al.length; i++) {
            long l = unmarshalSB4() & -1L;
            al[i] = l;
        }
    }

    public void marshalO2U(boolean flag) {
        if (flag) {
            marshalPTR();
        } else {
            marshalNULLPTR();
        }
    }

    public void marshalNULLPTR() {
        addPtr((byte) 0);
    }

    public void marshalPTR() {
        addPtr((byte) 1);
    }

    public void marshalCHR(byte[] ab) {
        marshalCHR(ab, 0, ab.length);
    }

    public void marshalCHR(byte[] ab, int offset, int len) {
        if (len > 0) {
            if (getTypeRep().isConvNeeded()) {
                marshalCLR(ab, offset, len);
            } else {
                writeBytes(ab, offset, len);
            }
        }
    }

    public void marshalCLR(byte[] ab, int i, int j) {
        if (j > TTCC_MXIN) {
            int i1 = 0;
            writeByte((byte) -2);
            do {
                int k = j - i1;
                int l = k <= TTCC_MXIN ? k : TTCC_MXIN;
                writeByte((byte) (l & 0xff));
                writeBytes(ab, i + i1, l);
                i1 += l;
            } while (i1 < j);
            writeByte((byte) 0);
        } else {
            writeByte((byte) (j & 0xff));
            if (ab.length != 0) {
                writeBytes(ab, i, j);
            }
        }
    }

    public void marshalCLR(byte[] ab, int i) {
        marshalCLR(ab, 0, i);
    }

    public void marshalKEYVAL(byte[][] ab0, int[] ai, byte[][] ab1, int[] ai1, byte[] ab2, int i) {
        for (int j = 0; j < i; j++) {
            if (ab0[j] != null && ai[j] > 0) {
                marshalUB4(ai[j]);
                marshalCLR(ab0[j], 0, ai[j]);
            } else {
                marshalUB4(0L);
            }
            if (ab1[j] != null && ai1[j] > 0) {
                marshalUB4(ai1[j]);
                marshalCLR(ab1[j], 0, ai1[j]);
            } else {
                marshalUB4(0L);
            }
            if (ab2[j] != 0) {
                marshalUB4(1L);
            } else {
                marshalUB4(0L);
            }
        }
    }

    public void marshalMap(Map<String, String> map) {
        byte[][] keys = new byte[map.size()][];
        byte[][] values = new byte[map.size()][];
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                keys[i] = this.getConversion().StringToCharBytes(entry.getKey());
                if (entry.getValue() != null) {
                    values[i] = this.getConversion().StringToCharBytes(entry.getValue());
                }
            }
            i++;
        }
        marshalKEYVAL(keys, values, new byte[map.size()], map.size());
    }

    public void marshalKEYVAL(byte[][] ab0, byte[][] ab1, byte[] ab2, int i) {
        int ai[] = new int[i];
        int ai1[] = new int[i];
        for (int j = 0; j < i; j++) {
            if (ab0[j] != null) {
                ai[j] = ab0[j].length;
            }
            if (ab1[j] != null) {
                ai1[j] = ab1[j].length;
            }
        }
        marshalKEYVAL(ab0, ai, ab1, ai1, ab2, i);
    }

    public void marshalDALC(byte[] ab) {
        if (ab == null || ab.length < 1) {
            writeByte((byte) 0);
        } else {
            marshalSB4(-1 & ab.length);
            marshalCLR(ab, ab.length);
        }
    }

    public byte unmarshalSB1() {
        return this.readByte();
    }

    public short unmarshalUB1() {
        return (short) (unmarshalSB1() & 0xff);
    }

    public short unmarshalSB2() {
        return (short) unmarshalUB2();
    }

    public int unmarshalUB2() {
        return buffer2Int((byte) 1);
    }

    public int unmarshalUCS2(byte[] abyte0, long l) {
        int i = unmarshalUB2();
        tmpBuffer2[0] = (byte) ((i & 0xff00) >> 8);
        tmpBuffer2[1] = (byte) (i & 0xff);
        if (l + 1L < (long) abyte0.length) {
            abyte0[(int) l] = tmpBuffer2[0];
            abyte0[(int) l + 1] = tmpBuffer2[1];
        }
        return tmpBuffer2[0] != 0 ? 3 : tmpBuffer2[1] != 0 ? 2 : 1;
    }

    public int unmarshalSB4() {
        return (int) unmarshalUB4();
    }

    public long unmarshalUB4() {
        return buffer2Long((byte) 2);
    }

    public int unmarshalSB4(byte[] abyte0) {
        long l = buffer2Value((byte) 2, abyte0);
        return (int) l;
    }

    public long unmarshalSB8() {
        return buffer2Long((byte) 2);
    }

    public int unmarshalRefCursor(byte[] abyte0) {
        int i = unmarshalSB4(abyte0);
        return i;
    }

    public int unmarshalSWORD() {
        return (int) unmarshalUB4();
    }

    public long unmarshalUWORD() {
        return unmarshalUB4();
    }

    public int unmarshalPTR() {
        return readPtr();
    }

    public byte[] unmarshalArrayWithNull() {
        int currentPosition = this.getPosition();
        while (this.readByte() != 0)
            ;
        int distPosition = this.getPosition();
        int lenght = distPosition - currentPosition - 1;
        byte[] result = new byte[lenght];
        System.arraycopy(buffer, currentPosition, result, 0, lenght);
        return result;
    }

    public byte[] unmarshalNBytes(int i) {
        byte abyte0[] = new byte[i];
        if (readBytes(abyte0, 0, abyte0.length) < 0) {
            throw new RuntimeException("无法从套接字读取更多的数据");
        }
        return abyte0;
    }

    public int unmarshalNBytes(byte abyte0[], int i, int j) {
        int k;
        for (k = 0; k < j; k += getNBytes(abyte0, i + k, j - k))
            ;
        return k;
    }

    public byte[] getNBytes(int i) {
        byte abyte0[] = new byte[i];
        if (readBytes(abyte0, 0, abyte0.length) < 0) {
            throw new RuntimeException("无法从套接字读取更多的数据");
        }
        return abyte0;
    }

    public int getNBytes(byte abyte0[], int i, int j) {
        int k = 0;
        if ((k = readBytes(abyte0, i, j)) < 0) {
            throw new RuntimeException("无法从套接字读取更多的数据");
        }
        return k;
    }

    public byte[] unmarshalCHR(int i) {
        byte abyte0[] = null;
        if (this.getTypeRep().isConvNeeded()) {
            abyte0 = unmarshalCLR(i, retLen);
            if (abyte0.length != retLen[0]) {
                byte abyte1[] = new byte[retLen[0]];
                System.arraycopy(abyte0, 0, abyte1, 0, retLen[0]);
                abyte0 = abyte1;
            }
        } else {
            abyte0 = getNBytes(i);
        }
        return abyte0;
    }

    public byte[] unmarshalCLR(int i, int[] ai) {
        byte abyte0[] = new byte[i * this.getConversion().c2sNlsRatio];
        unmarshalCLR(abyte0, 0, ai, i);
        return abyte0;
    }

    public void unmarshalCLR(byte[] ab, int i, int[] ai) {
        unmarshalCLR(ab, i, ai, 0x7fffffff);
    }

    public void unmarshalCLR(byte abyte0[], int i, int ai[], int j) {
        short word0 = 0;
        int k = i;
        boolean flag = false;
        int l = 0;
        word0 = unmarshalUB1();
        if (word0 < 0) {
            throw new RuntimeException("违反协议");
        }
        if (word0 == 0) {
            ai[0] = 0;
            return;
        }
        if (escapeSequenceNull(word0)) {
            ai[0] = 0;
            return;
        }
        if (word0 != TTCC_LNG) {
            int i1 = Math.min(j - l, word0);
            k = unmarshalBuffer(abyte0, k, i1);
            l += i1;
            int k1 = word0 - i1;
            if (k1 > 0) unmarshalBuffer(ignored, 0, k1);
        } else {
            byte byte1 = -1;
            do {
                if (byte1 != -1) {
                    word0 = unmarshalUB1();
                    if (word0 <= 0) {
                        break;
                    }
                }
                if (word0 == TTCC_LNG) {
                    switch (byte1) {
                        case -1:
                            byte1 = 1;
                            continue;
                        case 1:
                            byte1 = 0;
                            break;
                        case 0:
                            if (flag) {
                                byte1 = 0;
                                break;
                            }
                            byte1 = 0;
                            continue;
                        default:
                            break;
                    }
                }
                if (k == -1) {
                    unmarshalBuffer(ignored, 0, word0);
                } else {
                    int j1 = Math.min(j - l, word0);
                    k = unmarshalBuffer(abyte0, k, j1);
                    l += j1;
                    int l1 = word0 - j1;
                    if (l1 > 0) {
                        unmarshalBuffer(ignored, 0, l1);
                    }
                }
                byte1 = 0;
                if (word0 > TTCC_MXL) {
                    flag = true;
                }
            } while (true);
        }
        if (ai != null) {
            if (k != -1) {
                ai[0] = l;
            } else {
                ai[0] = abyte0.length - i;
            }
        }
    }

    public Map<String, String> unmarshalMap(int i) {
        Map<String, String> map = new HashMap<String, String>(i);
        byte[][] keys = new byte[i][];
        byte[][] values = new byte[i][];
        unmarshalKEYVAL(keys, values, i);
        for (int j = 0; j < i; j++) {
            String key = null;
            String value = null;
            if (keys[j] != null) {
                key = this.getConversion().CharBytesToString(keys[j], keys[j].length);
                if (values[j] != null) {
                    value = this.getConversion().CharBytesToString(values[j], values[j].length);
                }
            }
            if (key != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    public int unmarshalKEYVAL(byte[][] abyte0, byte[][] abyte1, int i) {
        byte[] abyte2 = new byte[1000];
        int[] ai = new int[1];
        int j = 0;
        for (int l = 0; l < i; l++) {
            int k = unmarshalSB4();
            if (k > 0) {
                unmarshalCLR(abyte2, 0, ai);
                abyte0[l] = new byte[ai[0]];
                System.arraycopy(abyte2, 0, abyte0[l], 0, ai[0]);
            }
            k = unmarshalSB4();
            if (k > 0) {
                unmarshalCLR(abyte2, 0, ai);
                abyte1[l] = new byte[ai[0]];
                System.arraycopy(abyte2, 0, abyte1[l], 0, ai[0]);
            }
            j = unmarshalSB4();
        }

        abyte2 = null;
        return j;
    }

    public int unmarshalBuffer(byte abyte0[], int i, int j) {
        if (j <= 0) {
            return i;
        }
        if (abyte0.length < i + j) {
            unmarshalNBytes(abyte0, i, abyte0.length - i);
            unmarshalNBytes(ignored, 0, (i + j) - abyte0.length);
            i = -1;
        } else {
            unmarshalNBytes(abyte0, i, j);
            i += j;
        }
        return i;
    }

    public byte[] unmarshalDALC() {
        long l = unmarshalUB4();
        byte[] abyte0 = new byte[(int) (-1L & l)];
        if (abyte0.length > 0) {
            abyte0 = unmarshalCLR(abyte0.length, retLen);
            if (abyte0 == null) {
                throw new RuntimeException("违反协议");
            }
        } else {
            abyte0 = new byte[0];
        }
        return abyte0;
    }

    public byte[] unmarshalDALC(int[] ai) {
        long l = unmarshalUB4();
        byte abyte0[] = new byte[(int) (-1L & l)];
        if (abyte0.length > 0) {
            abyte0 = unmarshalCLR(abyte0.length, ai);
            if (abyte0 == null) {
                throw new RuntimeException("违反协议");
            }
        } else {
            abyte0 = new byte[0];
        }
        return abyte0;
    }

    public long unmarshalDALC(byte[] ab, int i, int[] ai) {
        long l = unmarshalUB4();
        if (l > 0L) {
            unmarshalCLR(ab, i, ai);
        }
        return l;
    }

    public byte[] unmarshalTEXT(int i) {
        int j = 0;
        byte[] abyte0 = new byte[i];
        do {
            if (j >= i) {
                break;
            }
            if (readBytes(abyte0, j, 1) < 0) {
                throw new RuntimeException("无法从套接字读取更多的数据");
            }
        } while (abyte0[j++] != 0);

        byte[] abyte1;
        if (abyte0.length == --j) {
            abyte1 = abyte0;
        } else {
            abyte1 = new byte[j];
            System.arraycopy(abyte0, 0, abyte1, 0, j);
        }
        return abyte1;
    }

    @SuppressWarnings("unchecked")
    public byte[] unmarshalCLRforREFS() {
        short word1 = 0;
        byte[] abyte0 = null;
        Vector vector = new Vector(10, 10);
        short word2 = unmarshalUB1();
        if (word2 < 0) {
            throw new RuntimeException("违反协议");
        }
        if (word2 == 0) {
            return null;
        }
        if (!escapeSequenceNull(word2)) {
            if (word2 == TTCC_LNG) {
                do {
                    short word0;
                    if ((word0 = unmarshalUB1()) <= 0) {
                        break;
                    }
                    if (word0 != TTCC_LNG || !getTypeRep().isServerConversion()) {
                        word1 += word0;
                        byte[] abyte1 = new byte[word0];
                        unmarshalBuffer(abyte1, 0, word0);
                        vector.addElement(abyte1);
                    }
                } while (true);
            } else {
                word1 = word2;
                byte[] abyte2 = new byte[word2];
                unmarshalBuffer(abyte2, 0, word2);
                vector.addElement(abyte2);
            }
            abyte0 = new byte[word1];
            int i = 0;
            for (; vector.size() > 0; vector.removeElementAt(0)) {
                int j = ((byte[]) vector.elementAt(0)).length;
                System.arraycopy(vector.elementAt(0), 0, abyte0, i, j);
                i += j;
            }
        } else {
            abyte0 = null;
        }
        return abyte0;
    }

    public int processIndicator(boolean flag, int i) {
        short word0 = unmarshalSB2();
        int j = 0;
        if (!flag) {
            if (word0 == 0) {
                j = i;
            } else if (word0 == -2 || word0 > 0) {
                j = word0;
            } else {
                j = 0x10000 + word0;
            }
        }
        return j;
    }

    // ////////////////////////////////////////////////////////
    private boolean escapeSequenceNull(int i) {
        boolean flag = false;
        switch (i) {
            case 0:
                flag = true;
                break;
            case TTCC_ESC:
                throw new RuntimeException("违反协议");
            case TTCC_ERR:
                flag = true;
                break;
        }
        return flag;
    }

    private void addPtr(byte b) {
        if ((getTypeRep().getRep(4) & 1) > 0) {
            writeByte(b);
        } else {
            byte len = 0;
            for (int j = tmpBuffer4.length - 1; j >= 0; j--) {
                tmpBuffer4[len++] = (byte) ((b >>> (8 * j)) & 0xff);
            }
            if ((getTypeRep().getRep(4) & 2) > 0) {
                reverseArray(tmpBuffer4, len);
            }
            writeBytes(tmpBuffer4, 0, len);
        }
    }

    private int readPtr() {
        if ((getTypeRep().getRep(4) & 1) > 0) {
            readByte();
            return 1;
        } else {
            readBytes(tmpBuffer4, 0, tmpBuffer4.length);
            return tmpBuffer4.length;
        }
    }

    private long buffer2Long(int b) {
        int i = 0;
        boolean flag = false;
        if ((getTypeRep().getRep(b) & 1) > 0) {
            i = this.readByte() & 0xff;
            if ((i & 0x80) > 0) {
                i &= 0x7f;
                flag = true;
            }
            if (i < 0) {
                throw new RuntimeException("无法从套接字读取更多的数据");
            }
            if (i == 0) {
                return 0L;
            }
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
        readBytes(ab, 0, i);

        long l1 = 0L;
        for (int j = 0; j < ab.length; j++) {
            long l = 0L;
            if ((getTypeRep().getRep(b) & 2) > 0) {
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

    private int buffer2Int(byte b) {
        int i = 0;
        boolean flag = false;
        if ((getTypeRep().getRep(b) & 1) > 0) {
            i = this.readByte() & 0xff;
            if ((i & 0x80) > 0) {
                i &= 0x7f;
                flag = true;
            }
            if (i < 0) throw new RuntimeException("无法从套接字读取更多的数据");
            if (i == 0) return 0;
            if ((b == 1 && i > 2) || (b == 2 && i > 4)) {
                throw new RuntimeException("类型长度大于最大值");
            }
        } else if (b == 1) {
            i = 2;
        } else if (b == 2) {
            i = 4;
        }

        byte[] ab = getTmpBuffer(i);
        readBytes(ab, 0, i);

        int i1 = 0;
        for (int j = 0; j < ab.length; j++) {
            int i2 = 0;
            if ((getTypeRep().getRep(b) & 2) > 0) {
                i2 = ab[ab.length - 1 - j] & 0xff;
            } else {
                i2 = ab[j] & 0xff;
            }
            i1 |= i2 << (8 * (ab.length - 1 - j));
        }
        if (b != 2) {
            i1 &= 0xffff;
        }
        if (flag) i1 = -i1;
        return i1;
    }

    private long buffer2Value(byte b, byte[] buffer) {
        int i = 0;
        boolean flag = false;
        if ((getTypeRep().getRep(b) & 1) > 0) {
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
            if ((getTypeRep().getRep(b) & 2) > 0) {
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

    private byte int2Buffer(int i, byte[] ab, byte b) {
        boolean flag = true;
        byte b1 = 0;
        for (int j = ab.length - 1; j >= 0; j--) {
            ab[b1] = (byte) ((i >>> (8 * j)) & 0xff);
            if ((getTypeRep().getRep(b) & 1) > 0) {
                if (!flag || ab[b1] != 0) {
                    flag = false;
                    b1++;
                }
            } else {
                b1++;
            }
        }

        if ((getTypeRep().getRep(b) & 1) > 0) {
            writeByte(b1);
        }
        if ((getTypeRep().getRep(b) & 2) > 0) {
            reverseArray(ab, b1);
        }
        return b1;
    }

    private byte long2Buffer(long l, byte[] ab, byte b) {
        boolean flag = true;
        byte b1 = 0;
        for (int i = ab.length - 1; i >= 0; i--) {
            ab[b1] = (byte) ((l >>> (8 * i)) & 255L);
            if ((getTypeRep().getRep(b) & 1) > 0) {
                if (!flag || ab[b1] != 0) {
                    flag = false;
                    b1++;
                }
            } else {
                b1++;
            }
        }

        if ((getTypeRep().getRep(b) & 1) > 0) {
            writeByte(b1);
        }
        if ((getTypeRep().getRep(b) & 2) > 0) {
            reverseArray(ab, b1);
        }
        return b1;
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

    private void reverseArray(byte[] ab, byte b) {
        for (int i = 0; i < b / 2; i++) {
            byte b1 = ab[i];
            ab[i] = ab[b - 1 - i];
            ab[b - 1 - i] = b1;
        }
    }

    // ////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        // T4CPacketBuffer meg = new T4CPacketBuffer(2);
        // meg.marshalUB2(100);
        // byte[] ab = meg.toByteBuffer().array();
        // System.out.println(ByteUtil.toHex(ab, 0, ab.length));
        //
        // byte[] ab0 = { 0x64, 0x00 };
        // meg = new T4CPacketBuffer(ab0);
        // System.out.println(meg.unmarshalUB2());

    }
}
