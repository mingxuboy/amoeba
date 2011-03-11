/**
 * 
 */
package com.meidusa.amoeba.mongodb.handler.merge;

import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;

/**
 * 
 * @author Struct
 *
 */
public class CountFunctionMerge implements FunctionMerge{

	@Override
	public ResponseMongodbPacket mergeResponse(RequestMongodbPacket requestPacket,
			List<ResponseMongodbPacket> multiResponsePacket) {
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		BSONObject cmdResult = null;
		
		for(ResponseMongodbPacket response :multiResponsePacket){
			if(response.numberReturned > 0){
				if(cmdResult == null){
					cmdResult = response.documents.get(0);
				}else{
					Number value = (Number)cmdResult.get("n");
					Number add = (Number)response.documents.get(0).get("n");
					if(value != null && add != null){
						value = value.longValue() + add.longValue(); 
						cmdResult.put("n", value.doubleValue());
					}else{
						if(add != null){
							cmdResult.put("n", add.doubleValue());
						}
					}
				}
			}
		}
		
		result.documents = new ArrayList<BSONObject>();
		if(cmdResult != null){
			result.documents.add(cmdResult);
		}
		
		result.responseTo = requestPacket.requestID;
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
		
	}
	
}