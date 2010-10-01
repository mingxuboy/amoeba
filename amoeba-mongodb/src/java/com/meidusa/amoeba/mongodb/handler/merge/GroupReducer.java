package com.meidusa.amoeba.mongodb.handler.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ognl.Ognl;
import ognl.OgnlException;

import org.bson.BSONObject;

import com.meidusa.amoeba.util.ObjectUtil;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author struct
 *
 */
public class GroupReducer {
	protected Set<String> keys = null;
	private String[] reducerParams = new String[2];
	private String finalizeParam;
	private Object reduceExpression;
	private Object finalizeExpression;
	
	public void initial(BSONObject keys,String function,String finalize){
		if(keys != null){
			this.keys = keys.keySet();
		}
		if(function !=null){
			String temp[] = StringUtil.split(function, "{}");
			
			String parameters[] = StringUtil.split(temp[0].trim(),"(,)");
			reducerParams[0] = parameters[1].trim();
			reducerParams[1] = parameters[2].trim();
			try {
				String expression = temp[1].replaceAll(";", ",").trim();
				if(expression.endsWith(",")){
					expression = expression.substring(0, expression.length() -1);
				}
				reduceExpression = Ognl.parseExpression(expression);
			} catch (OgnlException e) {
				e.printStackTrace();
			}
		}
		
		if(finalize !=null){
			String temp[] = StringUtil.split(finalize, "{}");
			
			String parameters[] = StringUtil.split(temp[0].trim(),"(,)");
			finalizeParam = parameters[1].trim();
			try {
				String expression = temp[1].replaceAll(";", ",").trim();
				if(expression.endsWith(",")){
					expression = expression.substring(0, expression.length() -1);
				}
				finalizeExpression = Ognl.parseExpression(expression);
			} catch (OgnlException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public List<BSONObject> reduce(List<List<BSONObject>> inputObjs){
		List<BSONObject> output = null;
		for(List<BSONObject> inputs : inputObjs){
			if(inputs == null){
				continue;
			}
			
			if(output == null){
				output = inputs;
			}
			List<BSONObject> notMatchList = null;
			Map<String,BSONObject> root = new HashMap<String,BSONObject>(2);
			Map<String,Object> context = new HashMap<String,Object>();
			for(BSONObject input : inputs){
				_Match_:{
					for(BSONObject prev : output ){
						if(isMatch(input,prev)){
							root.put(reducerParams[0], input);
							root.put(reducerParams[1], prev);
							function(context,root);
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
			Map<String,BSONObject> root = new HashMap<String,BSONObject>(2);
			Map<String,Object> context = new HashMap<String,Object>();
			for(BSONObject prev : output ){
				root.put(finalizeParam, prev);
				finalize(context,root);
			}
		}
		
		return output;
	}
	
	protected boolean isMatch(BSONObject input,BSONObject prev){
		if(keys == null){
			return true;
		}
		for(String key : keys){
			if(!ObjectUtil.equals(input.get(key),prev.get(key))){
				return false;
			}
		}
		return true;
	}
	
	protected  void function(Map<String,Object> context,Map<String,BSONObject> root){
		try {
			Ognl.getValue(reduceExpression, context, root);
		} catch (OgnlException e) {
			e.printStackTrace();
		}
	}
	
	protected void finalize(Map<String,Object> context,Map<String,BSONObject> root){
		try {
			Ognl.getValue(finalizeExpression, context, root);
		} catch (OgnlException e) {
			e.printStackTrace();
		}
	}
}
