package com.meidusa.amoeba.sqljep;

import java.util.Stack;

import com.meidusa.amoeba.sqljep.function.Comparative;

/**
 * 该类目前只为内部系统设计 Comparative
 * 首先在第一次开始pop的时候记录每次pop出来的对象类型，如果连续pop的对象中包含有 Comparative对象，
 * 则下一次push的时候，就必须用Comparative进去，然后清理上一次pop的痕迹
 * pop .... pop ... push作为一个循环
 * @author struct
 *
 * @param <E>
 */
public class ComparativeStack extends Stack<Comparable> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Comparative lastComparative;
	private boolean autoBox = true;
	public boolean isAutoBox() {
		return autoBox;
	}

	public void setAutoBox(boolean autoBox) {
		this.autoBox = autoBox;
	}

	public Comparable<?> push(Comparable<?> item) {
		try{
			if(autoBox && lastComparative != null && !(item instanceof Comparative) ){
				lastComparative.setValue(item);
				return super.push(lastComparative);
			}else{
				return super.push(item);
			}
		}finally{
			if(autoBox){
				lastComparative = null;
			}
		}
		
	}

	public synchronized Comparable<?> pop() {
		Comparable<?> obj =super.pop();
		if(obj instanceof Comparative){
			if(autoBox){
				lastComparative = (Comparative)obj;
				obj = lastComparative.getValue();
			}
		}
		
		return obj;
	}
}
