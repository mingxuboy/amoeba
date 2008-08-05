package com.meidusa.amoeba.oracle.io;

import java.nio.ByteBuffer;

import com.meidusa.amoeba.net.io.PacketOutputStream;

public class OraclePacketOutputStream extends PacketOutputStream implements OraclePacketConstant{

	@Override
	protected void initHeader() {
		this._buffer.put(HEADER_PAD);
	}

	@Override
	public ByteBuffer returnPacketBuffer() {
		return null;
	}

}
