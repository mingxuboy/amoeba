package com.meidusa.amoeba.oracle.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.oracle.io.OraclePacketInputStream;
import com.meidusa.amoeba.oracle.io.OraclePacketOutputStream;

public abstract class OracleConnection extends DatabaseConnection {

	public OracleConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
		/**
		 * TODO
		 * 测试用
		 */
		this.setAuthenticated(true);
	}

	@Override
	protected PacketInputStream createPacketInputStream() {
		return new OraclePacketInputStream();
	}

	@Override
	protected PacketOutputStream createPakcetOutputStream() {
		return new OraclePacketOutputStream();
	}

	/**
	 * 为了提升性能，由于Oracle数据包写到目的地的时候已经包含了包头，则不需要经过PacketOutputStream处理
	 */
	public void postMessage(byte[] msg)
    {
        ByteBuffer out= ByteBuffer.allocate(msg.length);
        out.put(msg);
        out.flip();
        _outQueue.append(out);
        _cmgr.invokeConnectionWriteMessage(this);
    }
}
