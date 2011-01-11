package com.meidusa.amoeba.manager.client;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.Packet;

/**
 * 
 * @author Struct
 *
 * @param <T>
 */
public interface CallbackHandler<T extends Packet> {
	public void doCallBack(Connection conn, T packet);
}
