package com.meidusa.amoeba.mongodb.handler.merge;

import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;

import com.meidusa.amoeba.util.ObjectUtil;

/**
 * 
 * @author struct
 *
 */
public abstract class GroupReducer {
	protected List<String> keys = new ArrayList<String>();
	
	public abstract void initialKeys();
	
	public List<BSONObject> reduce(List<BSONObject>[] inputObjs){
		List<BSONObject> output = null;
		for(List<BSONObject> inputs : inputObjs){
			if(inputs == null){
				continue;
			}
			
			if(output == null){
				output = inputs;
			}
			List<BSONObject> notMatchList = null;
			for(BSONObject input : inputs){
				_Match_:{
					for(BSONObject prev : output ){
						if(isMatch(input,prev)){
							function(input,prev);
							break _Match_;
						}
					}
					
					if(notMatchList == null){
						notMatchList = new ArrayList<BSONObject>();
					}
					notMatchList.add(input);
				}
			}
			
			if(notMatchList != null){
				output.addAll(notMatchList);
			}
			
		}
		
		if(output != null){
			for(BSONObject prev : output ){
				finalize(prev);
			}
		}
		
		return output;
	}
	
	protected boolean isMatch(BSONObject input,BSONObject prev){
		for(String key : keys){
			if(!ObjectUtil.equals(input.get(key),prev.get(key))){
				return false;
			}
		}
		return true;
	}
	
	public abstract void function(BSONObject obj,BSONObject prev);
	
	public abstract void finalize(BSONObject prev);
}
