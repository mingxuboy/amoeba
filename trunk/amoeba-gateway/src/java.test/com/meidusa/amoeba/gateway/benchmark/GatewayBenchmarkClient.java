package com.meidusa.amoeba.gateway.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.benchmark.AbstractBenchmarkClient;
import com.meidusa.amoeba.benchmark.AbstractBenchmark.TaskRunnable;
import com.meidusa.amoeba.config.ConfigUtil;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.gateway.packet.AbstractGatewayPacket;
import com.meidusa.amoeba.gateway.packet.GatewayPacketConstant;
import com.meidusa.amoeba.gateway.packet.GatewayPingPacket;
import com.meidusa.amoeba.gateway.packet.GatewayPongPacket;
import com.meidusa.amoeba.gateway.packet.GatewayRequestPacket;
import com.meidusa.amoeba.gateway.packet.GatewayResponsePacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacket;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author Struct
 *
 */
public class GatewayBenchmarkClient extends AbstractBenchmarkClient<AbstractGatewayPacket> {
	private static Logger	logger        = Logger.getLogger(GatewayBenchmarkClient.class);
	public GatewayBenchmarkClient(Connection connection,CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task) {
		super(connection,requestLatcher,responseLatcher,task);
	}

	public boolean needPing(long now) {
		return false;
	}

	public AbstractGatewayPacket decodeRecievedPacket(byte[] message) {
		int type = AbstractGatewayPacket.getType(message);
		AbstractGatewayPacket packet = null;
		switch(type){
		case GatewayPacketConstant.PACKET_TYPE_PING:
			packet = new GatewayPingPacket();
			break;
		case GatewayPacketConstant.PACKET_TYPE_PONG:
			packet = new GatewayPongPacket();
			break;
		case GatewayPacketConstant.PACKET_TYPE_SERVICE_REQUEST:
			packet = new GatewayRequestPacket();
			break;
		case GatewayPacketConstant.PACKET_TYPE_SERVICE_RESPONSE:
			packet = new GatewayResponsePacket();
			break;
		default:
			logger.error("error type="+type+"\r\n"+StringUtil.dumpAsHex(message, message.length));
		}
		packet.init(message, this.getConnection());
		return packet;
	}

	final Map<String ,String > parameterMap = new HashMap<String,String>(); 
	final Map<String ,Object > beanParameterMap = new HashMap<String,Object>(); 
	
	public AbstractGatewayPacket createRequestPacket() {
		Properties properties = this.getRequestProperties();
		AbstractGatewayPacket packet = null;
		try {
			packet = (AbstractGatewayPacket)Class.forName((String)properties.get("class")).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		Map<String,Object> map = this.getNextRequestContextMap();
		ParameterMapping.mappingObjectField(packet, beanParameterMap,map,this, AbstractPacket.class);
		
		if(packet instanceof GatewayRequestPacket){
			
			Map<String ,String > _parameterMap_ = new HashMap<String,String>(); 
			for(Map.Entry<String, String> entry : parameterMap.entrySet()){
				String value = ConfigUtil.filterWtihOGNL(entry.getValue(), map,this);
				_parameterMap_.put(entry.getKey(), value);
			}
			GatewayRequestPacket request = (GatewayRequestPacket)packet;
			request.parameterMap = _parameterMap_;
		}
		
		return packet;
	}
	
	public void init(){
		super.init();
		Properties properties = this.getRequestProperties();
		for(Map.Entry<Object,Object> entry : properties.entrySet()){
			if(entry.getKey().toString().startsWith("parameterMap.")){
				parameterMap.put(entry.getKey().toString().substring("parameterMap.".length()), entry.getValue().toString());
			}else{
				beanParameterMap.put(entry.getKey().toString(), entry.getValue());
			}
		}
	}
	
	@Override
	public void startBenchmark() {
		AbstractGatewayPacket packet = this.createRequestPacket();
		getConnection().postMessage(packet.toByteBuffer(this.getConnection()));
	}
	
}
