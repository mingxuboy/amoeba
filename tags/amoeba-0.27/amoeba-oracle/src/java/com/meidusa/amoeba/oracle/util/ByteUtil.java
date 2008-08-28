package com.meidusa.amoeba.oracle.util;

public class ByteUtil {

    public static String toHex(byte[] b, int offset, int len) {
        StringBuffer s = new StringBuffer();
        for (int i = offset; i < (offset + len); i++) {
            s.append(String.format("%1$02x", (b[i] & 0xff)));
            s.append(" ");
        }
        return s.toString();
    }

    public static String fromHex(String hexString) {
        return fromHex(hexString, null);
    }

    public static String fromHex(String hexString, String charset) {
        try {
            String[] hex = hexString.split(" ");
            byte[] b = new byte[hex.length];
            for (int i = 0; i < hex.length; i++) {
                b[i] = (byte) (Integer.parseInt(hex[i], 16) & 0xff);
            }

            if (charset == null) {
                return new String(b);
            }
            return new String(b, charset);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        // byte[] b = { 1, 2, 3, 4, 50, 60, 70 };
        // System.out.println(toHex(b, 3, 4));

        String hex = "41 55 54 48 5f 53 45 53 53 4b 45 59";
        System.out.println(fromHex(hex, null));
    }
}
