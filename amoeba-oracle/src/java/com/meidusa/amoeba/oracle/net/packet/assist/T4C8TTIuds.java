package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

public class T4C8TTIuds {

    T4CTTIoac       udsoac;

    boolean         udsnull;
    short           udscnl;
    byte            optimizeOAC;
    byte            udscolnm[];
    short           udscolnl;
    byte            udssnm[];
    long            udssnl;
    int             snnumchar[];
    byte            udstnm[];
    long            udstnl;
    int             tnnumchar[];
    int             numBytes[];

    T4CPacketBuffer meg;

    public T4C8TTIuds(T4CPacketBuffer meg){
        this.meg = meg;
        udsoac = new T4CTTIoac(meg);
    }

    void unmarshal() {
        udsoac.unmarshal();
        short word0 = meg.unmarshalUB1();
        udsnull = word0 > 0;
        udscnl = meg.unmarshalUB1();
        numBytes = new int[1];
        udscolnm = meg.unmarshalDALC(numBytes);
        snnumchar = new int[1];
        udssnm = meg.unmarshalDALC(snnumchar);
        udssnl = udssnm.length;
        tnnumchar = new int[1];
        udstnm = meg.unmarshalDALC(tnnumchar);
        udstnl = udstnm.length;
    }

    byte[] getColumName() {
        return udscolnm;
    }

    byte[] getTypeName() {
        return udstnm;
    }

    byte[] getSchemaName() {
        return udssnm;
    }

    short getTypeCharLength() {
        return (short) tnnumchar[0];
    }

    short getColumNameByteLength() {
        return (short) numBytes[0];
    }

    short getSchemaCharLength() {
        return (short) snnumchar[0];
    }

}
