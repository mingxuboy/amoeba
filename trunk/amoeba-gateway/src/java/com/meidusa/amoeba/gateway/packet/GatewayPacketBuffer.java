package com.meidusa.amoeba.gateway.packet;

import java.nio.ByteOrder;

/**
 * 
 * @author Struct
 *
 */
public class GatewayPacketBuffer extends GenericIOPacketBuffer implements GatewayPacketConstant {

	private StringBuffer contentBuffer;
	public GatewayPacketBuffer(byte[] buf) {
		super(buf);
	}

	public GatewayPacketBuffer(int size){
        super(size);
    }

	public ByteOrder getByteOrder(){
		return ByteOrder.BIG_ENDIAN;
	}
	
	@Override
	public int getHeadSize() {
		return HEADER_SIZE;
	}

	@Override
	public long readFieldLength() {
		return this.readInt();
	}

	@Override
	public void writeFieldLength(int length) {
		this.writeInt(length);
	}
	
	public void appendProperty(String name,String value){
		if(contentBuffer == null){
			contentBuffer = new StringBuffer();
		}
		
		if(contentBuffer.length()>0){
			contentBuffer.append(PACKET_CONTENT_SPLITER);
		}
		contentBuffer.append(name).append("=").append(value);
	}
	
	public String getPacketContent(){
		if(contentBuffer == null){
			return null;
		}else{
			return contentBuffer.toString();
		}
	}
}
