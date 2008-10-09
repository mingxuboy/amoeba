package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

/**
 * 查询返回的描述信息数据包
 * 
 * @author hexianmao
 * @version 2008-10-9 下午10:10:50
 */
public class T4C8TTIuds {

    boolean   udsnull;
    short     udscnl;
    byte      optimizeOAC;
    byte      udscolnm[];
    short     udscolnl;
    byte      udssnm[];
    long      udssnl;
    int       snnumchar[];
    byte      udstnm[];
    long      udstnl;
    int       tnnumchar[];
    int       numBytes[];

    T4CTTIoac udsoac;

    public T4C8TTIuds(){
        udsoac = new T4CTTIoac();
    }

    public T4CTTIoac getUdsoac() {
        return udsoac;
    }

    void unmarshal(T4CPacketBuffer meg) {
        udsoac.unmarshal(meg);
        short word0 = meg.unmarshalUB1();
        udsnull = (word0 > 0);
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

    void marshal(T4CPacketBuffer meg) {
        udsoac.marshal(meg);
        if (udsnull) {
            meg.marshalUB1((short) 1);
        } else {
            meg.marshalUB1((short) 0);
        }
        meg.marshalUB1(udscnl);
        meg.marshalDALC(udscolnm);
        meg.marshalDALC(udssnm);
        meg.marshalDALC(udstnm);
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
