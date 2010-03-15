package com.meidusa.amoeba.sqljep.function;

import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.ParseException;

public class Var extends PostfixCommand implements Declare{
	private String identity;
	public Var(String identity){
		this.identity = identity;
	}
	
	@Override
	public Comparable<?>[] evaluate(ASTFunNode node, JepRuntime runtime)
			throws ParseException {
		node.childrenAccept(runtime.ev, null);
		Comparable<?>  param = runtime.stack.pop();
		return new Comparable<?>[]{param};
	}

	@Override
	public int getNumberOfParameters() {
		return 1;
	}

	@Override
	public Comparable<?> getResult(Comparable<?>... comparables)
			throws ParseException {
		return comparables[0];
	}
	
	public boolean isDeclare(){
		return true;
	}

	@Override
	public void declare(JepRuntime runtime,Comparable<?> comparable) {
		runtime.vars.put(identity, comparable);
	}

}
