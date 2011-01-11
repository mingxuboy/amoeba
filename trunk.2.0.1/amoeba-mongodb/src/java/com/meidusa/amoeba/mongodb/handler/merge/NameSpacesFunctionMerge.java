/**
 * 
 */
package com.meidusa.amoeba.mongodb.handler.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;

/**
 * 
 * @author Struct
 *
 */
public class NameSpacesFunctionMerge implements FunctionMerge{
	
	@Override
	public ResponseMongodbPacket mergeResponse(RequestMongodbPacket requestPacket,
			List<ResponseMongodbPacket> multiResponsePacket) {
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		
		Map<String,BSONObject> dbMap = new HashMap<String,BSONObject>();
		for(ResponseMongodbPacket response :multiResponsePacket){
			
			if(response.numberReturned > 0){
				for(BSONObject info: response.documents){
					String name = (String)info.get("name");
					BSONObject source = dbMap.get(name);
					if(source == null){
						dbMap.put(name, info);
					}
				}
			}
		}
		
		result.documents = new ArrayList<BSONObject>();
		for(BSONObject bson : dbMap.values()){
			result.documents.add(bson);
		}
		
		result.responseTo = requestPacket.requestID;
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
		
	}
	
}