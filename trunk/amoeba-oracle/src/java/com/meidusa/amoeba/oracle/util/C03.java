package com.meidusa.amoeba.oracle.util;

import java.util.Random;

public class C03 {

    public C03(){
        i = 971;
        h = 11113;
        g = 0x19782;
        f = 4181;
        e = false;
    }

    public short a() {
        if (!e) {
            b();
        } else {
            f += 7;
            h += 1907;
            g += 0x120d3;
            if (f >= 9973)
                f -= 9871;
            if (h >= 0x18697)
                h -= 0x15f85;
            if (g >= 0x36dd9)
                g -= 0x177e9;
            f = f * i + h + g;
        }
        return (short) (f >> 16 ^ f & 0xffff);
    }

    private void b() {
        Random random = new Random();
        f = random.nextInt();
        e = true;
    }

    public void c(byte abyte0[], int j) {
        for (int k = 0; k < j; k++)
            abyte0[k] = d();

    }

    public byte d() {
        return (byte) a();
    }

    boolean e;
    int     f;
    int     g;
    int     h;
    int     i;
}
