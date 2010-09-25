/**
 * 
 */
package com.meidusa.amoeba.mongodb.handler.merge;

import java.util.List;

import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;

public class GroupFunctionMerge implements FunctionMerge{

	@Override
	public ResponseMongodbPacket mergeResponse(RequestMongodbPacket requestPacket,
			List<ResponseMongodbPacket> multiResponsePacket) {
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		
		result.responseTo = requestPacket.requestID;
		
		/*if(cmd>0 || isFindOne){
			result.documents = new ArrayList<BSONObject>();
			if(cmdResult != null){
				result.documents.add(cmdResult);
			}
		}*/
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
		
	}
	
}