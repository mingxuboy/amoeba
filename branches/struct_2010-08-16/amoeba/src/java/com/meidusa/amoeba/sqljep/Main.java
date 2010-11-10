package com.meidusa.amoeba.sqljep;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.route.AbstractQueryRouter;
import com.meidusa.amoeba.route.TableRuleFileLoader;
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
		ComparativeBaseList ID =  new ComparativeOR();
		ID.addComparative(new Comparative(Comparative.Equivalent,50));
		ID.addComparative(new Comparative(Comparative.Equivalent,150));
		//Comparable<?> ID = 12;
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
		final RowJEP sqljep = new RowJEP("var hello=abs(hash(ID)) % 3; var isBool = false; " +
				"(case hello " +
	  			"	WHEN range(0,1,1,0) THEN 'server0' ;" +
	  			"	WHEN range(1,2,1,0) THEN 'server1' ;" +
	  			"	WHEN range(2,3,1,0) THEN 'server2' ;" +
	  			"END CASE;)");	
		sqljep.parseExpression(columnMapping,valMap,TableRuleFileLoader.ruleFunTab);
		result = sqljep.getValue(row);
		for(int j=0;j<0;j++){
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
				
				 System.out.println("total:"+(System.currentTimeMillis() -start));	
			}
		}.start();
		
		}
		result = sqljep.getValue(row);
		final long start = System.currentTimeMillis();
		for(int i=0;i<1;i++)
		result = sqljep.getValue(row);
		System.out.println(result+"\n"+"...total:"+(System.currentTimeMillis() -start));
		
	}
}