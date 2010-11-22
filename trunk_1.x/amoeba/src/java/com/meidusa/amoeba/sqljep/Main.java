package com.meidusa.amoeba.sqljep;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.route.AbstractQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;
import com.meidusa.amoeba.sqljep.function.ComparativeAND;
import com.meidusa.amoeba.sqljep.function.ComparativeBaseList;
import com.meidusa.amoeba.sqljep.function.ComparativeOR;
import com.meidusa.amoeba.sqljep.variable.Variable;



public class Main {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		ComparativeBaseList ID =  new ComparativeOR(Comparative.Equivalent,22);
		//Comparable<?> ID = 12;
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		/*ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));*/
		
		final Comparable<?>[] row = {ID,new Comparative(Comparative.GreaterThanOrEqual,50),new Comparative(Comparative.GreaterThanOrEqual,new java.util.Date()),"wwe"};
		HashMap<String,Integer> columnMapping = new HashMap<String,Integer>();
		columnMapping.put("ID",0);
		columnMapping.put("SUM",1);
		columnMapping.put("SALE_DATE",2);
		columnMapping.put("name",3);
		final Map<String,Variable> valMap = new HashMap<String,Variable>();
		valMap.put("age", new Variable(){
			public Comparable<?> getValue() {
				return "true";
			}
			
		});
		
		valMap.put("sysdate", new Variable(){
			public Comparable<?> getValue() {
				return new Date();
			}
			
		});
		Object result = null;
		System.out.println("wwe".hashCode()%500);
		final RowJEP sqljep = new RowJEP("var hello=abs(hash(ID)) % 32; var isBool = false; " +
				"(case hello " +
	  			"	WHEN range(0,1,1,0) THEN 'aadf0' ;" +
	  			"	WHEN range(1,2,1,0) THEN 'aadf1' ;" +
	  			"	WHEN range(2,3,1,0) THEN 'aadf2' ;" +
	  			"	WHEN range(3,4,1,0) THEN 'aadf3' ;" +
	  			"	WHEN range(4,5,1,0) THEN 'aadf4' ;" +
	  			"	WHEN range(5,6,1,0) THEN 'aadf5' ;" +
	  			"	WHEN range(6,7,1,0) THEN 'aadf6' ;" +
	  			"	WHEN range(7,8,1,0) THEN 'aadf7' ;" +
	  			"	WHEN range(8,9,1,0) THEN 'aadf8' ;" +
	  			"	WHEN range(9,10,1,0) THEN 'aadf9' ;" +
	  			"	WHEN range(10,11,1,0) THEN 'aadf10' ;" +
	  			"	WHEN range(11,12,1,0) THEN 'aadf11' ;" +
	  			"	WHEN range(12,13,1,0) THEN age1?'hell12':'aadf12' ;" +
	  			"	WHEN range(13,14,1,0) THEN 'aadf13' ;" +
	  			"	WHEN range(14,15,1,0) THEN 'aadf14' ;" +
	  			"	WHEN range(15,16,1,0) THEN 'aadf15' ;" +
	  			"	WHEN range(16,17,1,0) THEN 'aadf16' ;" +
	  			"	WHEN range(17,18,1,0) THEN 'aadf17' ;" +
	  			"	WHEN range(18,19,1,0) THEN 'aadf18' ;" +
	  			"	WHEN range(19,20,1,0) THEN 'aadf19' ;" +
	  			"	WHEN range(20,21,1,0) THEN 'aadf20' ;" +
	  			"	WHEN range(21,22,1,0) THEN 'aadf21' ;" +
	  			"	WHEN range(22,23,1,0) THEN 'aadf22' ;" +
	  			"	WHEN range(23,24,1,0) THEN 'aadf23' ;" +
	  			"	WHEN range(24,25,1,0) THEN 'aadf24' ;" +
	  			
	  			"	ELSE 'aadf25...' " +
	  			"END CASE;)|| 'qwerqer' || (12+12)");	
		sqljep.parseExpression(columnMapping,valMap,AbstractQueryRouter.ruleFunTab);
		result = sqljep.getValue(row);
		for(int j=0;j<100;j++){
		new Thread(){
			public void run(){
				final long start = System.currentTimeMillis();
				for(int i=0;i<1;i++){
				try {
					
					Object result = sqljep.getValue(row);
				   //System.out.println(result);
				   //sqljep.getValue(row);
					}
				catch (ParseException e) {
				   e.printStackTrace();
				}
				}
				
				 System.out.println("totle:"+(System.currentTimeMillis() -start));	
			}
		}.start();
		
		}
		result = sqljep.getValue(row);
		final long start = System.currentTimeMillis();
		for(int i=0;i<1000;i++)
		result = sqljep.getValue(row);
		System.out.println(result+"\n"+"...totle:"+(System.currentTimeMillis() -start));
		
	}
}