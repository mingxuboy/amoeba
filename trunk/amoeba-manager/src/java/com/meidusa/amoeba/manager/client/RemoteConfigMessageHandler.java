package com.meidusa.amoeba.manager.client;

import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.manager.net.packet.ManagerAbstractPacket;
import com.meidusa.amoeba.manager.net.packet.ObjectPacket;
import com.meidusa.amoeba.manager.net.packet.PingPacket;
import com.meidusa.amoeba.manager.net.packet.PongPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;

public class RemoteConfigMessageHandler implements MessageHandler,
		CallbackHandler<ManagerAbstractPacket> {

	private Map<Integer, CallbackHandler<ManagerAbstractPacket>> callbackHandlerMap = new HashMap<Integer, CallbackHandler<ManagerAbstractPacket>>();

	public RemoteConfigMessageHandler() {
		this.registerCallBack(ManagerAbstractPacket.FUN_TYPE_PING, this);
	}

	@Override
	public void handleMessage(Connection conn) {
		byte[] message = null;
		while ((message = conn.getInQueue().getNonBlocking()) != null) {
			byte type = message[4];
			ManagerAbstractPacket packet = null;
			switch (type) {
			case ManagerAbstractPacket.FUN_TYPE_PING:
				packet = new PingPacket();
				break;
			case ManagerAbstractPacket.FUN_TYPE_PONG:
				packet = new PongPacket();
				break;
			case ManagerAbstractPacket.FUN_TYPE_DBSERVER_ADD:
			case ManagerAbstractPacket.FUN_TYPE_DBSERVER_DELETE:
			case ManagerAbstractPacket.FUN_TYPE_DBSERVER_DSIABLE:
			case ManagerAbstractPacket.FUN_TYPE_DBSERVER_ENABLE:
			case ManagerAbstractPacket.FUN_TYPE_DBSERVER_UPDATE:
			case ManagerAbstractPacket.FUN_TYPE_AMOEBA_SHUTDOWN:
			case ManagerAbstractPacket.FUN_TYPE_AMOEBA_RELOAD:
			case ManagerAbstractPacket.FUN_TYPE_RULE_UPDATE:
			case ManagerAbstractPacket.FUN_TYPE_RULE_ADD:
			case ManagerAbstractPacket.FUN_TYPE_RULE_DELETE:
				packet = new ObjectPacket();
				break;
			}
			
			if(packet != null){
				packet.init(message, conn);
				CallbackHandler<ManagerAbstractPacket> handler  = callbackHandlerMap.get(packet.funType);
				handler.doCallBack(conn,packet);
			}

		}
	}

	public void registerCallBack(int type,
			CallbackHandler<ManagerAbstractPacket> handler) {
		callbackHandlerMap.put(type, handler);
	}

	public void removeCallBack(
			CallbackHandler<? extends ManagerAbstractPacket> handler) {
		for (Map.Entry<Integer, CallbackHandler<ManagerAbstractPacket>> entry : callbackHandlerMap
				.entrySet()) {
			if (entry.getValue() == handler) {
				callbackHandlerMap.remove(entry.getKey());
				return;
			}
		}
	}

	public void removeCallBack(int type) {
		callbackHandlerMap.remove(type);
	}

	/**
	 * RemoteConfigMessageHandler Ö»¸ºÔðping /pong
	 */
	@Override
	public void doCallBack(Connection conn, ManagerAbstractPacket packet) {
		if(packet.funType == ManagerAbstractPacket.FUN_TYPE_PING){
			conn.postMessage(new PongPacket().toByteBuffer(conn));
		}
	}
}
