package com.meidusa.amoeba.gateway.packet;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.net.packet.AbstractPacket;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author Struct
 */
public abstract class AbstractGatewayPacket extends AbstractPacket<GatewayPacketBuffer> implements GatewayPacketConstant {
	private static final long serialVersionUID = 1L;
	protected int packetLength;//
	public short version;//4
	public short contentType;//json=0 , xml =1
	public int type;//
	public int clientId;//4
	public int clientTransId;//4
	public long clientUserID;//8
	
	
	@Override
	protected void afterPacketWritten(GatewayPacketBuffer buffer) {
		int position = buffer.getPosition();
		packetLength = position;
        buffer.setPosition(0);
        buffer.writeInt(packetLength);
        buffer.setPosition(position);
	}

	@Override
	protected int calculatePacketSize() {
		return HEADER_SIZE;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class getPacketBufferClass() {
		return GatewayPacketBuffer.class ;
	}

	protected void writeHead(GatewayPacketBuffer buffer){
		buffer.setPosition(0);
		buffer.writeInt(0);
		// modified by Sun Ning. Add various version support. 2010-04-20
		buffer.writeShort(version);
		buffer.writeShort(contentType);
        buffer.writeInt(type);
        buffer.writeInt(clientId);
        buffer.writeInt(clientTransId);
        buffer.writeLong(clientUserID);
       
        buffer.setPosition(HEADER_SIZE);
	}
	
	protected void readHead(GatewayPacketBuffer buffer) {
		packetLength = buffer.readInt();
		version = buffer.readShort();
		contentType = buffer.readShort();
		type = buffer.readInt();
		clientId = buffer.readInt();
		clientTransId = buffer.readInt();
		clientUserID = buffer.readLong();
	}
	
	protected void readBody(GatewayPacketBuffer buffer) {
		if(PACKET_CONTENT_ALL_IN_ONE){
			byte[] byts = buffer.getBytes(packetLength - 44);
			if(byts.length >0){
				String content = null;
				try {
					content = new String(byts,PACKET_CHARSET);
				} catch (UnsupportedEncodingException e) {
					content = new String(byts);
				}
				
				if(content !=null){
					Map<String,Object> properties = new HashMap<String,Object>();
					String[] tmp = StringUtil.split(content,PACKET_CONTENT_SPLITER);
					for(int i = 0;i<tmp.length;i++){
						String[] pair = StringUtil.split(tmp[i],"=");
						if(pair.length ==2){
							properties.put(pair[0], pair[1]);
						}
					}
					fillFieldContent(this,properties,this.getClass());
				}
			}
		}
	}
	
	private void fillFieldContent(Object object,Map<String,Object> properties,Class<?> clazz){
		
		if(clazz !=  AbstractGatewayPacket.class && clazz != Object.class && clazz != null){
			Field[] fields = clazz.getDeclaredFields();
			for(int i=0;i<fields.length;i++){
				String name = fields[i].getName();
				if((fields[i].getModifiers() & Modifier.STATIC) ==0 &&  (fields[i].getModifiers() & Modifier.PUBLIC) !=0){
					Object value = properties.get(name);
					
					if(value != null){
						try {
							if(ParameterMapping.isPrimitiveType(fields[i].getType())){
								value = ParameterMapping.deStringize(fields[i].getType(), (String)value);
								fields[i].setAccessible(true);
								fields[i].set(object, value);
							}else{
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				}
			}
			fillFieldContent(object,properties,clazz.getSuperclass());
		}
	}
	
	@SuppressWarnings("unused")
	private void wirteFieldConent(GatewayPacketBuffer buffer,Class<?> clazz){
		if(clazz !=  AbstractGatewayPacket.class && clazz != Object.class && clazz != null){
			Field[] fields = clazz.getDeclaredFields();
			for(int i=0;i<fields.length;i++){
				String name = fields[i].getName();
				if((fields[i].getModifiers() & Modifier.STATIC) ==0 &&  (fields[i].getModifiers() & Modifier.PUBLIC) !=0){
					try {
						Object obj = fields[i].get(this);
						if(obj != null){
							String value = obj.toString();
							buffer.appendProperty(name, value);
						}
					} catch (Exception e) {
					}
				}
			}
			wirteFieldConent(buffer,clazz.getSuperclass());
		}
	}
	
	public static int getType(byte[] buf){
		byte[] typeBytes = new byte[4];
		int i=0;
		typeBytes[i] = buf[i+TYPE_POSITION];
		typeBytes[++i] = buf[i+TYPE_POSITION];
		typeBytes[++i] = buf[i+TYPE_POSITION];
		typeBytes[++i] = buf[i+TYPE_POSITION];
		return ByteBuffer.wrap(typeBytes).getInt();
	}
	
	public static void main(String [] args){
		System.out.println(getType(new byte[]{(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)10,(byte)1,(byte)1,}));
	}
}
