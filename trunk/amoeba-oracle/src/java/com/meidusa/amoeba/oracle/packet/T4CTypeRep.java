package com.meidusa.amoeba.oracle.packet;

public class T4CTypeRep {

    byte[] rep = new byte[5];

    T4CTypeRep(){
        init();
    }

    private void init() {
        rep[0] = 0;
        rep[1] = 1;
        rep[2] = 1;
        rep[3] = 1;
        rep[4] = 1;
    }

    void setRep(byte position, byte value) {
        if (position < 0 || position > 4 || value > 3) {
            throw new RuntimeException("无效的类型表示 ");
        }
        rep[position] = value;
    }

    byte getRep(byte position) {
        if (position < 0 || position > 4) {
            throw new RuntimeException("无效的类型表示 ");
        }
        return rep[position];
    }
}
