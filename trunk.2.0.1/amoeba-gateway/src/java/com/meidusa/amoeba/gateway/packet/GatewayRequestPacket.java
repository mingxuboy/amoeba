package com.meidusa.amoeba.gateway.packet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Struct
 *
 */
public class GatewayRequestPacket extends AbstractGatewayPacket {
	private static final long serialVersionUID = 1L;
	public String apiName;
	public Map<String,String> parameterMap;
	public String authorizationString;
	
	public GatewayRequestPacket(){
		type =  PACKET_TYPE_SERVICE_REQUEST;
	}
	protected void writeBody(GatewayPacketBuffer buffer) throws UnsupportedEncodingException {
		buffer.writeLengthCodedString(apiName, GatewayPacketConstant.PACKET_CHARSET);
		if(parameterMap != null){
			StringBuffer sb = new StringBuffer();
			for(Map.Entry<String, String> entry : parameterMap.entrySet()){
				sb.append(entry.getKey()).append("=");
				if(entry.getValue() != null){
					sb.append(URLEncoder.encode(entry.getValue(), GatewayPacketConstant.PACKET_CHARSET));
				}
				sb.append(GatewayPacketConstant.PACKET_CONTENT_SPLITER);
			}
			buffer.writeLengthCodedString(sb.toString(), GatewayPacketConstant.PACKET_CHARSET);
		}else{
			buffer.writeLengthCodedString(null, GatewayPacketConstant.PACKET_CHARSET);
		}
		
		// write authorization charset
		if(this.version == VERSION_2){
			if(authorizationString != null){
				buffer.writeLengthCodedString(authorizationString, PACKET_CHARSET);
			}
		}
	}
	
	@Override
	protected void readBody(GatewayPacketBuffer buffer) {
		apiName = buffer.readLengthCodedString(GatewayPacketConstant.PACKET_CHARSET);
		String parameters = buffer.readLengthCodedString(GatewayPacketConstant.PACKET_CHARSET);
		parameterMap = new HashMap<String,String>();
		if(parameters != null){
			String[] args = parameters.split(GatewayPacketConstant.PACKET_CONTENT_SPLITER);
			if(args != null && args.length>0){
				for(String arg : args){
					String[] argTuple = arg.split("=");
					String value = argTuple.length>1? argTuple[1]: "";
					try {
						parameterMap.put(argTuple[0], URLDecoder.decode(value, GatewayPacketConstant.PACKET_CHARSET) );
					} catch (UnsupportedEncodingException e) {
						parameterMap.put(argTuple[0],value);
					}
				}
			}
		}
		// add by Sun Ning/2010-04-20
		if(this.version == VERSION_2 && buffer.hasRemaining()){
			
			authorizationString = buffer.readLengthCodedString(PACKET_CHARSET);
		}
	}
	
//	public static void main(String[] args){
//		try {
//			
//			"=%E5%A4%A9%E5%93%AA%E6%88%91%E6%B2%A1%E4%BA%8B%E5%B9%B2".getBytes("GBK");
//			"=%E5%A4%A9%E5%93%AA%E6%88%91%E6%B2%A1%E4%BA%8B%E5%B9%B2".getBytes("utf-8");
//			System.out.println(URLDecoder.decode("=%E5%A4%A9%E5%93%AA%E6%88%91%E6%B2%A1%E4%BA%8B%E5%B9%B2", PacketConstant.PACKET_CHARSET));
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		ServiceRequestPacket packet = new ServiceRequestPacket();
//		packet.apiName = "aaaa";
//		packet.parameterMap = new HashMap<String,String>();
//		packet.parameterMap.put("appid", 1234+"");
//		packet.parameterMap.put("appName", "������û�¸�");
//		byte[] bytes = packet.toByteBuffer().array();
//		packet.init(bytes);
//		System.out.println(packet);
//		
//	}
	
}
