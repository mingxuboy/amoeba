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
		//Comparable<?> ID = 29;
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,2456));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,12323));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,1212345));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,456));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,12));
		
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,12333));
		
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
		ID.addComparative(new Comparative(Comparative.Equivalent,27894));
		ID.addComparative(new Comparative(Comparative.Equivalent,6));
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
		Object result = null;
		System.out.println("wwe".hashCode()%500);
		final RowJEP sqljep = new RowJEP("var hello=abs(hash(ID)) % 1024 % 32;" +
				"(case " +
	  			"	WHEN (hello >= 0 and hello<1) THEN 'aadf0' ;" +
	  			"	WHEN (hello >=1 and hello <2) THEN 'aadf1' ;" +
	  			"	WHEN (hello >=2 and hello <3) THEN 'aadf2' ;" +
	  			"	WHEN (hello >=3 and hello <4) THEN 'aadf3' ;" +
	  			"	WHEN (hello >=4 and hello <5) THEN 'aadf4' ;" +
	  			"	WHEN (hello >=5 and hello <6) THEN 'aadf5' ;" +
	  			"	WHEN (hello >=6 and hello <7) THEN 'aadf6' ;" +
	  			"	WHEN (hello >=7 and hello <8) THEN 'aadf7' ;" +
	  			"	WHEN (hello >=8 and hello <9) THEN 'aadf8' ;" +
	  			"	WHEN (hello >=9 and hello <10) THEN 'aadf9' ;" +
	  			"	WHEN (hello >=10 and hello <11) THEN 'aadf10' ;" +
	  			"	WHEN (hello >=11 and hello <12) THEN 'aadf11' ;" +
	  			"	WHEN (hello >=12 and hello <13) THEN 'aadf12' ;" +
	  			"	WHEN (hello >=13 and hello <14) THEN 'aadf13' ;" +
	  			"	WHEN (hello >=14 and hello <15) THEN 'aadf14' ;" +
	  			"	WHEN (hello >=15 and hello <16) THEN 'aadf15' ;" +
	  			"	WHEN (hello >=16 and hello <17) THEN 'aadf16' ;" +
	  			"	WHEN (hello >=17 and hello <18) THEN 'aadf17' ;" +
	  			"	WHEN (hello >=18 and hello <19) THEN 'aadf18' ;" +
	  			"	WHEN (hello >=19 and hello <20) THEN 'aadf19' ;" +
	  			"	WHEN (hello >=20 and hello <21) THEN 'aadf20' ;" +
	  			"	WHEN (hello >=21 and hello <22) THEN 'aadf21' ;" +
	  			"	WHEN (hello >=22 and hello <23) THEN 'aadf22' ;" +
	  			"	WHEN (hello >=23 and hello <24) THEN 'aadf23' ;" +
	  			"	WHEN (hello >=24 and hello <25) THEN 'aadf24' ;" +
	  			
	  			"	ELSE 'aadf25...' " +
	  			"END CASE;)|| 'qwerqer' || (12+12)");	
		sqljep.parseExpression(columnMapping,valMap,AbstractQueryRouter.ruleFunTab);
		result = sqljep.getValue(row);
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
		
		}
		
		//final RowJEP sqljep = new RowJEP("var aa=1+2; 1=2");
		final RowJEP sqljep1 = new RowJEP("var hello=abs(hash(ID)) % 32;" +
				"(CASE " +
	  			"	WHEN (hello >= 0 and hello<1) THEN 'aadf0' ;" +
	  			"	WHEN (hello >=1 and hello <2) THEN 'aadf1' ;" +
	  			"	WHEN (hello >=2 and hello <3) THEN 'aadf2' ;" +
	  			"	WHEN (hello >=3 and hello <4) THEN 'aadf3' ;" +
	  			"	WHEN (hello >=4 and hello <5) THEN 'aadf4' ;" +
	  			"	WHEN (hello >=5 and hello <6) THEN 'aadf5' ;" +
	  			"	WHEN (hello >=6 and hello <7) THEN 'aadf6' ;" +
	  			"	WHEN (hello >=7 and hello <8) THEN 'aadf7' ;" +
	  			"	WHEN (hello >=8 and hello <9) THEN 'aadf8' ;" +
	  			"	WHEN (hello >=9 and hello <10) THEN 'aadf9' ;" +
	  			"	WHEN (hello >=10 and hello <11) THEN 'aadf10' ;" +
	  			"	WHEN (hello >=11 and hello <12) THEN 'aadf11' ;" +
	  			"	WHEN (hello >=12 and hello <13) THEN 'aadf12' ;" +
	  			"	WHEN (hello >=13 and hello <14) THEN 'aadf13' ;" +
	  			"	WHEN (hello >=14 and hello <15) THEN 'aadf14' ;" +
	  			"	WHEN (hello >=15 and hello <16) THEN 'aadf15' ;" +
	  			"	WHEN (hello >=16 and hello <17) THEN 'aadf16' ;" +
	  			"	WHEN (hello >=17 and hello <18) THEN 'aadf17' ;" +
	  			"	WHEN (hello >=18 and hello <19) THEN 'aadf18' ;" +
	  			"	WHEN (hello >=19 and hello <20) THEN 'aadf19' ;" +
	  			"	WHEN (hello >=20 and hello <21) THEN 'aadf20' ;" +
	  			"	WHEN (hello >=21 and hello <22) THEN 'aadf21' ;" +
	  			"	WHEN (hello >=22 and hello <23) THEN 'aadf22' ;" +
	  			"	WHEN (hello >=23 and hello <24) THEN 'aadf23' ;" +
	  			"	WHEN (hello >=24 and hello <25) THEN 'aadf24' ;" +
	  			
	  			"	ELSE 'aadf25...' " +
	  			"END CASE;)|| 'qwerqer' || (12+12)");	
		
		sqljep1.parseExpression(columnMapping,valMap,AbstractQueryRouter.ruleFunTab);
		result = sqljep1.getValue(row);
		final long start = System.currentTimeMillis();
		for(int i=0;i<10000;i++)
		result = sqljep1.getValue(row);
		System.out.println(result+"\n"+"totle:"+(System.currentTimeMillis() -start));
		
	}
}