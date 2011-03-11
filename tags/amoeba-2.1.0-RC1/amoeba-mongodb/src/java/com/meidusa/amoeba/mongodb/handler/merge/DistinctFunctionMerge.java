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
public class DistinctFunctionMerge implements FunctionMerge{

	@Override
	public ResponseMongodbPacket mergeResponse(RequestMongodbPacket requestPacket,
			List<ResponseMongodbPacket> multiResponsePacket) {
		ResponseMongodbPacket result = null;
		List retval = new ArrayList();
		for(ResponseMongodbPacket response : multiResponsePacket){
			if(response.numberReturned >0 && response.documents != null){
				if(result == null){
					result = response;
					retval = (List)response.documents.get(0).get("values");
				}else{
					retval.addAll((List)response.documents.get(0).get("values"));
				}
				
			}
		}
		BSONObject res = result.documents.get(0);
		res.put("values",retval);
		
		result.responseTo = requestPacket.requestID;
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
		
	}
	
}