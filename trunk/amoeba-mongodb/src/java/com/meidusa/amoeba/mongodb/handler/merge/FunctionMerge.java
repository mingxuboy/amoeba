/**
 * 
 */
package com.meidusa.amoeba.mongodb.handler.merge;

import java.util.List;

import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;

public interface FunctionMerge{
	public ResponseMongodbPacket mergeResponse(RequestMongodbPacket requestPacket,List<ResponseMongodbPacket> multiResponsePacket);
}