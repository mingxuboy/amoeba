package com.meidusa.amoeba.gateway.packet;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author Struct
 *
 */
public class GatewayResponsePacket extends AbstractGatewayPacket{
	private static final long serialVersionUID = 1L;
	public String content;
	
	public GatewayResponsePacket(){
		this.type = PACKET_TYPE_SERVICE_RESPONSE;
	}
	
	@Override
	protected void writeBody(GatewayPacketBuffer buffer)
			throws UnsupportedEncodingException {
		buffer.writeLengthCodedString(content, GatewayPacketConstant.PACKET_CHARSET);
	}
	protected void readBody(GatewayPacketBuffer buffer) {
		content = buffer.readLengthCodedString(GatewayPacketConstant.PACKET_CHARSET);
	}
}
