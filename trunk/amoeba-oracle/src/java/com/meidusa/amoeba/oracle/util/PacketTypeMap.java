package com.meidusa.amoeba.oracle.util;

import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;

public class PacketTypeMap {

    private static Map<Integer, String> map = new HashMap<Integer, String>();

    static {
        map.put(SQLnetDef.NS_PACKT_TYPE_CONNECT, "NS_PACKT_TYPE_CONNECT");
        map.put(SQLnetDef.NS_PACKT_TYPE_ACCEPT, "NS_PACKT_TYPE_ACCEPT");
        map.put(SQLnetDef.NS_PACKT_TYPE_ACK, "NS_PACKT_TYPE_ACK");
        map.put(SQLnetDef.NS_PACKT_TYPE_REFUTE, "NS_PACKT_TYPE_REFUTE");
        map.put(SQLnetDef.NS_PACKT_TYPE_REDIRECT, "NS_PACKT_TYPE_REDIRECT");
        map.put(SQLnetDef.NS_PACKT_TYPE_DATA, "NS_PACKT_TYPE_DATA");
        map.put(SQLnetDef.NS_PACKT_TYPE_NULL, "NS_PACKT_TYPE_NULL");
        map.put(SQLnetDef.NS_PACKT_TYPE_ABORT, "NS_PACKT_TYPE_ABORT");
        map.put(SQLnetDef.NS_PACKT_TYPE_RESEND, "NS_PACKT_TYPE_RESEND");
        map.put(SQLnetDef.NS_PACKT_TYPE_MARKER, "NS_PACKT_TYPE_MARKER");
        map.put(SQLnetDef.NS_PACKT_TYPE_ATTENTION, "NS_PACKT_TYPE_ATTENTION");
        map.put(SQLnetDef.NS_PACKT_TYPE_CONTROL, "NS_PACKT_TYPE_CONTROL");
        map.put(SQLnetDef.NS_PACKT_TYPE_HI, "NS_PACKT_TYPE_HI");
    }

    public static String get(int i) {
        return map.get(i);
    }

    public static void main(String[] args) {
        System.out.println(PacketTypeMap.get(1));
    }

}
