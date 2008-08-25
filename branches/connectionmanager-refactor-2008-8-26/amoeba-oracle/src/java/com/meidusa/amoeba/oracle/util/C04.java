package com.meidusa.amoeba.oracle.util;

public class C04 {

    public byte[] a(byte abyte0[], int i1) {
        e(abyte0, i1);
        return t;
    }

    private void b() {
        char ac[] = new char[65];
        char ac1[] = new char[65];
        byte abyte0[] = new byte[128];
        int i1 = (short) (E + 7) / 8;
        int j1 = (short) (s + 7) / 8;
        o = (short) j1;
        y = s / 16 + 1;
        v = new byte[o];
        d(abyte0, i1);
        abyte0[0] &= 255 >>> i1 - 8 * E;
        C08.d(ac, y, F, j1);
        C08.d(m, y, abyte0, i1);
        C08.d(q, y, r, j1);
        C08.k(ac1, ac, m, q, y);
        C08.r(v, o, ac1, y);
    }

    public C04(byte abyte0[], byte abyte1[], int i1){
        A = null;
        i = 0;
        C = null;
        D = 0;
        F = null;
        r = null;
        q = new char[65];
        m = new char[65];
        f(abyte0, abyte1, i1);
    }

    private void c(int i1) {
        int j1 = 0;
        do {
            if (j1 >= h.length)
                break;
            if (i1 >= h[j1] && i1 <= n[j1]) {
                E = k[j1];
                s = w[j1];
                F = new byte[(s + 7) / 8];
                r = new byte[(s + 7) / 8];
                if (i * 8 >= s && D * 8 >= s) {
                    System.arraycopy(A, 0, F, 0, F.length);
                    System.arraycopy(C, 0, r, 0, r.length);
                } else {
                    System.arraycopy(B[j1], 0, F, 0, F.length);
                    System.arraycopy(x[j1], 0, r, 0, r.length);
                }
                break;
            }
            j1++;
        } while (true);
        if (F != null)
            if (r != null)
                ;
    }

    C04(int i1){
        A = null;
        i = 0;
        C = null;
        D = 0;
        F = null;
        r = null;
        q = new char[65];
        m = new char[65];
        f(null, null, i1);
    }

    public C04(byte abyte0[], byte abyte1[], short word0, short word1){
        A = null;
        i = 0;
        C = null;
        D = 0;
        F = null;
        r = null;
        q = new char[65];
        m = new char[65];
        if (abyte0 != null && abyte1 != null) {
            F = abyte0;
            r = abyte1;
            s = word1;
            E = word0;
        } else {
            f(abyte0, abyte1, 40);
        }
    }

    private void d(byte abyte0[], int i1) {
        (new C03()).c(abyte0, i1);
    }

    private void e(byte abyte0[], int i1) {
        char ac[] = new char[65];
        char ac1[] = new char[65];
        j = o;
        t = new byte[j];
        C08.d(ac, y, abyte0, i1);
        C08.k(ac1, ac, m, q, y);
        C08.r(t, j, ac1, y);
    }

    private void f(byte abyte0[], byte abyte1[], int i1) {
        A = abyte0;
        if (abyte0 != null)
            i = abyte0.length;
        else
            i = 0;
        C = abyte1;
        if (abyte1 != null)
            D = abyte1.length;
        else
            D = 0;
        c(i1);
    }

    public byte[] g() {
        b();
        return v;
    }

    private static final short h[]   = { 40, 41, 56, 128, 256 };
    private int                i;
    private int                j;
    private static final short k[]   = { 80, 112, 112, 512, 512 };
    private static final byte  l[]   = { -36, -114, -93, 27, 8, 96, 105, -118, -52, -10, -47, -98, -121, 14, 52, -4, 103, -59, 89, 11, 78, -90, -79, 60, -43, -3, -17, 21, -84, -99, 95, 63, 33, 76, -36, 7, -52, -121, 74, -77, 1, -41, 127, 44, 67, 51, 81, 60, -34, 11, 30, -50, 100, 71, 118, 87, 92, 81, -52, -104, -77, -2, -25, -17 };
    private char               m[];
    private static final short n[]   = { 40, 64, 56, 128, 256 };
    private int                o;
    private static final byte  p[]   = { 2, 83, -77, -14, -90, -115, 61, -69, 106, -61, -103, 9, -64, -41, 4, 5, -14, 91, -126, 97, 107, 122, -24, -36, 29, 123, 3, -106, 53, -30, -37, -17, 67, 102, -6, -48, 76, -63 };
    private char               q[];
    private byte               r[];
    private short              s;
    private byte               t[];
    private byte               v[];
    private static final short w[]   = { 300, 512, 512, 512, 512 };
    private int                y;
    private static final byte  z[]   = { -126, -104, -34, 73, -34, -9, 9, -27, -32, 13, -80, -96, -91, -100, -87, -14, 61, -10, -58, -89, -23, 74, 68, -93, -31, -121, 46, -11, 76, 31, -95, 122, -33, 92, -14, 117, -127, -19, 81, -61, 38, -18, -117, -31, 4, 3, 30, 103, 80, 83, -75, 124, 75, 69, 111, 21, 74, 23, 86, 11, 90, 21, -107, -91 };
    private byte               A[];
    private static final byte  B[][] = { p, z, z, z, z };
    private byte               C[];
    private int                D;
    private short              E;
    private byte               F[];
    private static final byte  G[]   = { 12, 54, -127, -73, 4, 71, 3, -96, 120, 96, 81, 38, -116, -22, -101, -68, -93, 62, 124, 1, -85, 54, -117, 34, 117, -104, 119, 102, 53, -59, -128, -43, 36, -46, 80, 99, -72, -13 };
    private static final byte  x[][] = { G, l, l, l, l };
}
