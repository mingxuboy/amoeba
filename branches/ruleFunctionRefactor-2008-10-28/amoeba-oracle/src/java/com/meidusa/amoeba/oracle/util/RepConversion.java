package com.meidusa.amoeba.oracle.util;

/**
 * @author hexianmao
 * @version 2008-8-15 ÏÂÎç03:29:10
 */
public class RepConversion {

    public RepConversion(){
    }

    public static byte nibbleToHex(byte byte0) {
        byte0 &= 0xf;
        return (byte) (byte0 >= 10 ? (byte0 - 10) + 65 : byte0 + 48);
    }

    public static byte asciiHexToNibble(byte byte0) {
        byte byte1;
        if (byte0 >= 97 && byte0 <= 102) byte1 = (byte) ((byte0 - 97) + 10);
        else if (byte0 >= 65 && byte0 <= 70) byte1 = (byte) ((byte0 - 65) + 10);
        else if (byte0 >= 48 && byte0 <= 57) byte1 = (byte) (byte0 - 48);
        else byte1 = byte0;
        return byte1;
    }

    public static void bArray2Nibbles(byte abyte0[], byte abyte1[]) {
        for (int i = 0; i < abyte0.length; i++) {
            abyte1[i * 2] = nibbleToHex((byte) ((abyte0[i] & 0xf0) >> 4));
            abyte1[i * 2 + 1] = nibbleToHex((byte) (abyte0[i] & 0xf));
        }

    }

    public static String bArray2String(byte abyte0[]) {
        StringBuffer stringbuffer = new StringBuffer(abyte0.length * 2);
        for (int i = 0; i < abyte0.length; i++) {
            stringbuffer.append((char) nibbleToHex((byte) ((abyte0[i] & 0xf0) >> 4)));
            stringbuffer.append((char) nibbleToHex((byte) (abyte0[i] & 0xf)));
        }

        return stringbuffer.toString();
    }

    public static byte[] nibbles2bArray(byte abyte0[]) {
        byte abyte1[] = new byte[abyte0.length / 2];
        for (int i = 0; i < abyte1.length; i++) {
            abyte1[i] = (byte) (asciiHexToNibble(abyte0[i * 2]) << 4);
            abyte1[i] |= asciiHexToNibble(abyte0[i * 2 + 1]);
        }

        return abyte1;
    }

    public static byte[] toHex(long l) {
        byte byte0 = 16;
        byte abyte0[] = new byte[byte0];
        for (int i = byte0 - 1; i >= 0; i--) {
            abyte0[i] = nibbleToHex((byte) (int) (l & 15L));
            l >>= 4;
        }

        return abyte0;
    }

    public static byte[] toHex(int i) {
        byte byte0 = 8;
        byte abyte0[] = new byte[byte0];
        for (int j = byte0 - 1; j >= 0; j--) {
            abyte0[j] = nibbleToHex((byte) (i & 0xf));
            i >>= 4;
        }

        return abyte0;
    }

    public static byte[] toHex(short word0) {
        byte byte0 = 4;
        byte abyte0[] = new byte[byte0];
        for (int i = byte0 - 1; i >= 0; i--) {
            abyte0[i] = nibbleToHex((byte) (word0 & 0xf));
            word0 >>= 4;
        }

        return abyte0;
    }

}
