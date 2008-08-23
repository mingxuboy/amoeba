package com.meidusa.amoeba.sqljep.function;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ComparativeBaseList extends Comparative{
	
	protected List<Comparative> list = new ArrayList<Comparative>();
	public ComparativeBaseList(int function, Comparable<?> value) {
		super(function, value);
		list.add(new Comparative(function,value));
	}
	
	public ComparativeBaseList(Comparative item){
		super(item.getComparison(),item.getValue());
		list.add(item);
	}
	
	public void addComparative(Comparative item){
		this.list.add(item);
	}
	
	@SuppressWarnings("unchecked")
	public abstract boolean intersect(int function,Comparable other,Comparator comparator);

}
