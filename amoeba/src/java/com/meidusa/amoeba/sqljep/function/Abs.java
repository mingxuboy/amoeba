/*****************************************************************************
      SQLJEP - Java SQL Expression Parser 0.2
      November 1 2006
         (c) Copyright 2006, Alexey Gaidukov
      SQLJEP Author: Alexey Gaidukov

      SQLJEP is based on JEP 2.24 (http://www.singularsys.com/jep/)
           (c) Copyright 2002, Nathan Funk
 
      See LICENSE.txt for license information.
*****************************************************************************/

package com.meidusa.amoeba.sqljep.function;

import java.math.BigDecimal;

import com.meidusa.amoeba.sqljep.function.PostfixCommand;
import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.ParseException;

public class Abs extends PostfixCommand {
	final public int getNumberOfParameters() {
		return 1;
	}
	
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		Comparable<?>  param = runtime.stack.pop();
		runtime.stack.push(abs(param));
	}

	public static Comparable<?>  abs(Comparable<?>  param1) throws ParseException {
		
		Comparable<?> param = param1;
		Comparative returnValue =  (param instanceof Comparative)?((Comparative) param):null;
		if(returnValue != null){
			param = returnValue.getValue();
		}
		
		Comparable<?> result = null;
		if (param == null) {
			return null;
		}
		if (param instanceof String) {
			param = parse((String)param);
		}
		if (param instanceof BigDecimal) {		// BigInteger is not supported
			result = ((BigDecimal)param).abs();
		}
		if (param instanceof Double || param instanceof Float) {
			result = new Double(Math.abs(((Number)param).doubleValue()));
		}
		if (param instanceof Number) {		// Long, Integer, Short, Byte 
			result = new Long(Math.abs(((Number)param).longValue()));
		}
		if(result != null){
			if(returnValue != null){
				returnValue.setValue(result);
				return returnValue;
			}else{
				return result;
			}
		}
		throw new ParseException(WRONG_TYPE+" abs("+param.getClass()+")");
	}
}

