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
import com.meidusa.amoeba.sqljep.ASTArray;
import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.Node;
import com.meidusa.amoeba.sqljep.ParseException;

public final class In extends PostfixCommand {
	final public int getNumberOfParameters() {
		return 2;
	}
	
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.jjtGetChild(0).jjtAccept(runtime.ev, null);
		runtime.stack.setAutoBox(false);
		try{
			Comparable<?>  source = runtime.stack.pop();
			if (source == null) {
				runtime.stack.push(Boolean.FALSE);
			} else {
				Node arg = node.jjtGetChild(1);
				if (arg instanceof ASTArray) {
					arg.jjtAccept(runtime.ev, null);
					for (Comparable<?>  d : runtime.stack) {
						if(source instanceof Comparative){
							Comparative other = (Comparative) source;
							boolean result = other.intersect(Comparative.Equivalent, d, ComparativeComparator.comparator);
							if(result){
								runtime.stack.setSize(0);
								runtime.stack.push(Boolean.TRUE);
								return;
							}
						}else if (d != null && ComparativeComparator.compareTo(source, d) == 0) {
							runtime.stack.setSize(0);
							runtime.stack.push(Boolean.TRUE);
							return;
						}
					}
					runtime.stack.setSize(0);
					runtime.stack.push(Boolean.FALSE);
				} else {
					throw new ParseException("Internal error in function IN");
				}
			}
		}finally{
			runtime.stack.setAutoBox(true);
		}
	}
}

