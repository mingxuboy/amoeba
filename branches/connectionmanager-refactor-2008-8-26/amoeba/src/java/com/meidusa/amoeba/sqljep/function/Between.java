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

import com.meidusa.amoeba.sqljep.function.Comparative;
import com.meidusa.amoeba.sqljep.function.ComparativeComparator;
import com.meidusa.amoeba.sqljep.function.PostfixCommand;
import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.ParseException;

public final class Between extends PostfixCommand {
	final public int getNumberOfParameters() {
		return 3;
	}
	
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		Comparable<?>  limit2 = runtime.stack.pop();
		Comparable<?>  limit1 = runtime.stack.pop();
		Comparable<?>  source = runtime.stack.pop();
		if (source == null || limit1 == null || limit2 == null) {
			runtime.stack.push(Boolean.FALSE);
		} else {
			if(source instanceof Comparative){
				Comparative other = (Comparative) source;
				boolean result = other.intersect(Comparative.GreaterThanOrEqual, limit1, ComparativeComparator.comparator);
				result = result && other.intersect(Comparative.LessThanOrEqual, limit2, ComparativeComparator.comparator); 
				runtime.stack.push(result);
			}else{
				runtime.stack.push(
						ComparativeComparator.compareTo(source, limit1) >= 0 && 
						ComparativeComparator.compareTo(source, limit2) <= 0
				);
			}
		}
	}

}

