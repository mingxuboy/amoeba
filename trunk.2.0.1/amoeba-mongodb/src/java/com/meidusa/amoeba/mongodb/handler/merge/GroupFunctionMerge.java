/**
 * 
 */
package com.meidusa.amoeba.mongodb.handler.merge;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.map.LRUMap;
import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.packet.QueryMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.RequestMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;

/**
 * 
 * @author Struct
 */
public class GroupFunctionMerge implements FunctionMerge{
	private static LRUMap groupReducerMap = new LRUMap(1000);
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseMongodbPacket mergeResponse(RequestMongodbPacket requestPacket,
			List<ResponseMongodbPacket> multiResponsePacket) {
		QueryMongodbPacket queryPacket = (QueryMongodbPacket)requestPacket;
		ResponseMongodbPacket result = null;
		List inputs = new ArrayList();
		for(ResponseMongodbPacket response : multiResponsePacket){
			if(response.numberReturned >0 && response.documents != null){
				List list = (List)response.documents.get(0).get("retval");
				if(list != null && list.size()>0){
					if(result == null){
						result  = response;
					}
					inputs.add(list);
				}
			}
		}
		BSONObject groupBSONObject = (BSONObject)queryPacket.query.get("group");
		
		BSONObject keys = (BSONObject)groupBSONObject.get("key");
		String reduce = (String)groupBSONObject.get("$reduce");
		String finalize = (String)groupBSONObject.get("finalize");
		
		
		long key = (keys != null?(keys.hashCode()<<16):0)+(reduce != null?(reduce.hashCode()<<8):0)+(finalize != null?(finalize.hashCode()):0);
		GroupReducer groupReducer = (GroupReducer) groupReducerMap.get(key);
		if(groupReducer == null){
			synchronized (groupReducerMap) {
				groupReducer = (GroupReducer) groupReducerMap.get(key);
				if(groupReducer == null){
					groupReducer = new GroupReducer();
					groupReducer.initial(keys, reduce, finalize);
					groupReducerMap.put(key, groupReducer);
				}
			}
		}
		
		BSONObject res = result.documents.get(0);
		res.put("retval",groupReducer.reduce(inputs));
		
		result.responseTo = requestPacket.requestID;
		result.numberReturned = (result.documents == null?0:result.documents.size());
		return result;
		
	}
	
}