package com.meidusa.amoeba.gateway.packet;

/**
 * 
 * @author Struct
 *
 */
public class GatewayPongPacket extends AbstractGatewayPacket {
	private static final long serialVersionUID = 1L;
	public GatewayPongPacket(){
		this.type = GatewayPacketConstant.PACKET_TYPE_PONG;
	}
	
	@Override
	protected void readBody(GatewayPacketBuffer buffer) {
		
	}

}
