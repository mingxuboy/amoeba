package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import org.apache.commons.pool.ObjectPool;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.oracle.context.OracleProxyRuntimeContext;
import com.meidusa.amoeba.oracle.handler.OracleMessageHandler;
import com.meidusa.amoeba.oracle.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.packet.DataPacket;
import com.meidusa.amoeba.oracle.packet.Packet;
import com.meidusa.amoeba.oracle.packet.ResendPacket;

public class OracleClientConnection extends OracleConnection {

    public OracleClientConnection(SocketChannel channel, long createStamp){
        super(channel, createStamp);
        String defaultPoolName = OracleProxyRuntimeContext.getInstance().getQueryRouter().getDefaultPool();
        ObjectPool pool = OracleProxyRuntimeContext.getInstance().getPoolMap().get(defaultPoolName);
        try {
            Connection dst = (Connection) pool.borrowObject();
            new OracleMessageHandler(this, dst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(Connection conn, byte[] message) {
        this.getMessageHandler().handleMessage(conn, message);

//        Packet packet = null;
//        switch (message[4]) {
//            case Packet.NS_PACKT_TYPE_CONNECT:
//                packet = new ConnectPacket();
//                break;
//            case Packet.NS_PACKT_TYPE_ACCEPT:
//                packet = new AcceptPacket();
//                break;
//            case Packet.NS_PACKT_TYPE_ACK:
//                break;
//            case Packet.NS_PACKT_TYPE_REFUTE:
//                break;
//            case Packet.NS_PACKT_TYPE_REDIRECT:
//                break;
//            case Packet.NS_PACKT_TYPE_DATA:
//                packet = new DataPacket();
//                break;
//            case Packet.NS_PACKT_TYPE_NULL:
//                break;
//            case Packet.NS_PACKT_TYPE_ABORT:
//                break;
//            case Packet.NS_PACKT_TYPE_RESEND:
//                packet = new ResendPacket();
//                break;
//            case Packet.NS_PACKT_TYPE_MARKER:
//                break;
//            case Packet.NS_PACKT_TYPE_ATTENTION:
//                break;
//            case Packet.NS_PACKT_TYPE_CONTROL:
//                break;
//            case Packet.NS_PACKT_TYPE_HI:
//                break;
//            default:
//                throw new RuntimeException("unknowing packet type:" + message[4]);
//        }
//        if (packet != null) {
//            packet.init(message);
//        }
    }

}
