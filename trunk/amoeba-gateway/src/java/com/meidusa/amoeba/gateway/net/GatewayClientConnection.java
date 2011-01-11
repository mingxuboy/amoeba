package com.meidusa.amoeba.gateway.net;

import java.nio.channels.SocketChannel;
import java.util.Date;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.gateway.io.GatewayInputStream;
import com.meidusa.amoeba.gateway.io.GatewayOutputStream;
import com.meidusa.amoeba.gateway.packet.AbstractGatewayPacket;
import com.meidusa.amoeba.gateway.packet.GatewayPacketConstant;
import com.meidusa.amoeba.gateway.packet.GatewayPingPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;


/**
 * 
 * @author Struct
 *
 */
public class GatewayClientConnection extends Connection {
	private static Logger       logger        = Logger.getLogger(GatewayClientConnection.class);
	private long lastPingTime = System.currentTimeMillis();
	private long lastPongTime = System.currentTimeMillis();
	private long pingInterval = 10 * 1000;
	public GatewayClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	@Override
	protected PacketInputStream createPacketInputStream() {
		return new GatewayInputStream(true);
	}

	@Override
	protected PacketOutputStream createPacketOutputStream() {
		return new GatewayOutputStream(true);
	}

	public boolean needPing(long now) {
		/*return (((now - lastPingTime)>pingInterval) 
				&& (now - lastMessageSent > pingInterval));*/
		return false;
	}

    protected void doReceiveMessage(byte[] msg) {
    	
		int type = AbstractGatewayPacket.getType(msg);
		if (type == GatewayPacketConstant.PACKET_TYPE_PONG) {
			lastPongTime = System.currentTimeMillis();
			if(logger.isDebugEnabled()){
				logger.debug("receive pong packet from "+this.getSocketId());
			}
		}
		super.doReceiveMessage(msg);
    }

    public void ping(long now) {
		postMessage(new GatewayPingPacket().toByteBuffer(null));
		lastPingTime = System.currentTimeMillis();
		if(logger.isDebugEnabled()){
			logger.debug("send ping packet to "+this.getSocketId());
		}
	}
	
	public boolean checkIdle(long now) {
		boolean idle = super.checkIdle(now);
		if(!idle){
			if(lastPingTime - lastPongTime > 2 * pingInterval){
				logger.warn("receive pong packet timeout, id="+this.getSocketId()+",lastPingTime="+new Date(lastPingTime)+",lastPongTime ="+new Date(lastPongTime));
				return true;
			}
		}
		return false;
		
	}
}
