package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import org.apache.commons.pool.ObjectPool;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.oracle.context.OracleProxyRuntimeContext;
import com.meidusa.amoeba.oracle.handler.OracleMessageHandler;
import com.meidusa.amoeba.oracle.packet.Packet;
import com.meidusa.amoeba.oracle.packet.ResendPacket;

public class OracleClientConnection extends OracleConnection {

    private String     defaultPoolName = null;
    private ObjectPool pool            = null;
    private int        msgCount        = 0;

    public OracleClientConnection(SocketChannel channel, long createStamp){
        super(channel, createStamp);
        defaultPoolName = OracleProxyRuntimeContext.getInstance().getQueryRouter().getDefaultPool();
        pool = OracleProxyRuntimeContext.getInstance().getPoolMap().get(defaultPoolName);
        switchHandler();
    }

    public void handleMessage(Connection conn, byte[] message) {
        msgCount++;

        Packet packet = null;
        if (msgCount == 1) {
            if (message[4] == Packet.NS_PACKT_TYPE_CONNECT) {
                packet = new ResendPacket();
            } else {
                throw new RuntimeException("Error data packet.");
            }
        }
        // if (msgCount == 2) {
        // if (message[4] == Packet.NS_PACKT_TYPE_CONNECT) {
        // packet = new AcceptPacket();
        // } else {
        // throw new RuntimeException("Error data packet.");
        // }
        // }

        postMessage(packet.toByteBuffer().array());
        switchHandler();

        // Packet packet = null;
        // switch (message[4]) {
        // case Packet.NS_PACKT_TYPE_CONNECT:
        // packet = new ConnectPacket();
        // break;
        // case Packet.NS_PACKT_TYPE_ACCEPT:
        // packet = new AcceptPacket();
        // break;
        // case Packet.NS_PACKT_TYPE_ACK:
        // break;
        // case Packet.NS_PACKT_TYPE_REFUTE:
        // break;
        // case Packet.NS_PACKT_TYPE_REDIRECT:
        // break;
        // case Packet.NS_PACKT_TYPE_DATA:
        // packet = new DataPacket();
        // break;
        // case Packet.NS_PACKT_TYPE_NULL:
        // break;
        // case Packet.NS_PACKT_TYPE_ABORT:
        // break;
        // case Packet.NS_PACKT_TYPE_RESEND:
        // packet = new ResendPacket();
        // break;
        // case Packet.NS_PACKT_TYPE_MARKER:
        // break;
        // case Packet.NS_PACKT_TYPE_ATTENTION:
        // break;
        // case Packet.NS_PACKT_TYPE_CONTROL:
        // break;
        // case Packet.NS_PACKT_TYPE_HI:
        // break;
        // default:
        // throw new RuntimeException("unknowing packet type:" + message[4]);
        // }
        // if (packet != null) {
        // packet.init(message);
        // }

    }

    private void switchHandler() {
        try {
            Connection dst = (Connection) pool.borrowObject();
            new OracleMessageHandler(this, dst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
