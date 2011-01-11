package com.meidusa.amoeba.mongodb.interceptor;

import com.meidusa.amoeba.net.packet.AbstractPacket;

@SuppressWarnings("unchecked")
public interface PacketInterceptor<T extends AbstractPacket> {
	public boolean doIntercept(T packet);
}
