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
public class GetCollectionFunctionMerge implements FunctionMerge{
	
	public static Number addNumber(BSONObject source, BSONObject info,String name){
		Number sourceNumber = (Number)source.get(name);
		Number infoNumber = (Number)info.get(name);
		if(sourceNumber != null && infoNumber != null){
			sourceNumber = MergeMath.add(sourceNumber,infoNumber);
		}else{
			if(sourceNumber == null){
				sourceNumber = infoNumber;
			}
		}
		source.put(name, sourceNumber);
		return sourceNumber;
	}
	
	@Override
	public ResponseMongodbPacket mergeResponse(RequestMongodbPacket requestPacket,
			List<ResponseMongodbPacket> multiResponsePacket) {
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		BSONObject cmdResult = null;
		
		BSONObject source = null;
		for(ResponseMongodbPacket response :multiResponsePacket){
			
			if(response.numberReturned > 0){
				
				BSONObject responseResult = response.documents.get(0);
				if(cmdResult == null){
					cmdResult = responseResult;
				}
				
				BSONObject collection = (BSONObject)responseResult.get("retval");
				if(source == null){
					source = collection;
				}else{
					Number count = addNumber(source, collection, "count");
					Number size = addNumber(source, collection, "size");
					addNumber(source, collection, "storageSize");
					addNumber(source, collection, "totalIndexSize");
					source.put("paddingFactor",MergeMath.div(addNumber(source, collection, "paddingFactor"), 2));
					source.put("avgObjSize",MergeMath.div(size,count));
				}
			}
		}
		
		cmdResult.put("retval", source);
		
		result.documents = new ArrayList<BSONObject>();
		if(cmdResult != null){
			result.documents.add(cmdResult);
		}
		
		result.responseTo = requestPacket.requestID;
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
		
	}
	
}