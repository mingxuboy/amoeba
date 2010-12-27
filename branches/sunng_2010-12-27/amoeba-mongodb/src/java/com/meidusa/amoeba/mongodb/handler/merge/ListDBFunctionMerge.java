/**
 * 
 */
package com.meidusa.amoeba.mongodb.handler.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BSONObject;
import org.bson.BasicDBList;
import org.bson.types.BasicBSONList;

import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author Struct
 *
 */
public class ListDBFunctionMerge implements FunctionMerge{
	
	public static void merge(BSONObject source, BSONObject info){
		if(StringUtil.equals((String)source.get("name"), (String)info.get("name"))){
			double sizeOndisk1 = Double.valueOf((Double)source.get("sizeOnDisk"));
			double sizeOndisk2 = Double.valueOf((Double)info.get("sizeOnDisk"));
			source.put("sizeOnDisk", (sizeOndisk1 + sizeOndisk2));
			source.put("empty", ((Boolean)source.get("empty") || (Boolean)info.get("empty")));
		}
	}
	
	@Override
	public ResponseMongodbPacket mergeResponse(RequestMongodbPacket requestPacket,
			List<ResponseMongodbPacket> multiResponsePacket) {
		ResponseMongodbPacket result = new ResponseMongodbPacket();
		BSONObject cmdResult = null;
		Map<String,BSONObject> dbMap = new HashMap<String,BSONObject>();
		for(ResponseMongodbPacket response :multiResponsePacket){
			
			if(response.numberReturned > 0){
				BSONObject nextResult = response.documents.get(0);
				
				BasicBSONList databases = (BasicBSONList)nextResult.get("databases");
				for(Object object: databases){
					BSONObject info = (BSONObject)object;
					String name = (String)info.get("name");
					BSONObject source = dbMap.get(name);
					if(source == null){
						dbMap.put(name, info);
					}else{
						merge(source,info);
					}
				}
				
				if(cmdResult == null){
					cmdResult = nextResult;
				}else{
					double totalSize = (Double)cmdResult.get("totalSize");
					double totalSize1 = totalSize + (Double)nextResult.get("totalSize");
					cmdResult.put("totalSize", totalSize1);
				}
			}
		}
		
		result.documents = new ArrayList<BSONObject>();
		if(cmdResult != null){
			BasicDBList listDB = new BasicDBList();
			for(BSONObject bson : dbMap.values()){
				listDB.add(bson);
			}
			cmdResult.put("databases", listDB);
			result.documents.add(cmdResult);
		}
		
		result.responseTo = requestPacket.requestID;
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
		
	}
	
}