package com.meidusa.amoeba.gateway.packet;

/**
 * 
 * @author Struct
 */
public class GatewayPingPacket extends AbstractGatewayPacket {
	private static final long serialVersionUID = 1L;

	public GatewayPingPacket(){
		this.type = GatewayPacketConstant.PACKET_TYPE_PING;
	}
}
