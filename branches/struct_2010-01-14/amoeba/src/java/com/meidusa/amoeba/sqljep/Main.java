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
		ComparativeBaseList ID =  new ComparativeOR(Comparative.GreaterThanOrEqual,8);
		//Comparable ID = 6;
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		final Comparable<?>[] row = {ID,new Comparative(Comparative.GreaterThanOrEqual,50),new Comparative(Comparative.GreaterThanOrEqual,new java.util.Date()),"wwe"};
		HashMap<String,Integer> columnMapping = new HashMap<String,Integer>();
		columnMapping.put("ID",0);
		columnMapping.put("SUM",1);
		columnMapping.put("SALE_DATE",2);
		columnMapping.put("name",3);
		final Map<String,Variable> valMap = new HashMap<String,Variable>();
		valMap.put("age", new Variable(){
			public Comparable<?> getValue() {
				return "223";
			}
			
		});
		
		valMap.put("sysdate", new Variable(){
			public Comparable<?> getValue() {
				return new Date();
			}
			
		});
		
		System.out.println("wwe".hashCode()%500);
		/*final RowJEP sqljep = new RowJEP("ID in (1,2,4,3,8) and hash(ID)>=4 and 1=1 and SUM =100 and  trunc(SALE_DATE) < to_date('2009-04-03','yyyy-mm-dd')  and abs(hash(name))%500<300" );
		
		sqljep.parseExpression(columnMapping,valMap,AbstractQueryRouter.ruleFunTab);
		
		for(int j=0;j<100;j++){
		new Thread(){
			public void run(){
				final long start = System.currentTimeMillis();
				for(int i=0;i<1000;i++){
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
		
		}*/
		
		//final RowJEP sqljep = new RowJEP("var aa=1+2; 1=2");
		RowJEP sqljep = new RowJEP("(case " +
	  			"	WHEN (ID >= 0 and ID<1) THEN 'aadf0' ;" +
	  			"	WHEN (ID >=1 and ID <2) THEN 'aadf1' ;" +
	  			"	WHEN (ID >=2 and ID <3) THEN 'aadf2' ;" +
	  			"	WHEN (ID >=3 and ID <4) THEN 'aadf3' ;" +
	  			"	WHEN (ID >=4 and ID <5) THEN 'aadf4' ;" +
	  			"	WHEN (ID >=5 and ID <6) THEN 'aadf5' ;" +
	  			"	WHEN (ID >=6 and ID <7) THEN 'aadf6' ;" +
	  			"	WHEN (ID >=7 and ID <8) THEN 'aadf7' ;" +
	  			"	WHEN (ID >=8 and ID <9) THEN 'aadf8' ;" +
	  			"	ELSE 'aadf9...' " +
	  			"END CASE;)|| 'qwerqer' || (12+12)");
		
		sqljep.parseExpression(columnMapping,valMap,AbstractQueryRouter.ruleFunTab);
		Object result = sqljep.getValue(row);
		System.out.println(result);
		
	}
}