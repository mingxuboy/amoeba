/*****************************************************************************
      SQLJEP - Java SQL Expression Parser 0.2
      November 1 2006
         (c) Copyright 2006, Alexey Gaidukov
      SQLJEP Author: Alexey Gaidukov

      SQLJEP is based on JEP 2.24 (http://www.singularsys.com/jep/)
           (c) Copyright 2002, Nathan Funk
 
      See LICENSE.txt for license information.
*****************************************************************************/

package com.meidusa.amoeba.sqljep;

import java.util.Calendar;
import java.text.DateFormatSymbols;

import com.meidusa.amoeba.util.StaticString;
import com.meidusa.amoeba.util.ThreadLocalMap;


final public class JepRuntime {
	
	public ComparativeStack stack = new ComparativeStack();
	
	public Calendar calendar;
	public DateFormatSymbols dateSymbols;
	public ParserVisitor ev;
	public Comparable[] row;
	public JepRuntime(ParserVisitor visitor) {
		ev = visitor;
		if (calendar == null) {
			calendar = Calendar.getInstance();
		}
		dateSymbols = (DateFormatSymbols)ThreadLocalMap.get(StaticString.DATE_FORMAT_SYMBOLS);
		if (dateSymbols == null) {
			dateSymbols = new DateFormatSymbols();
			ThreadLocalMap.put(StaticString.DATE_FORMAT_SYMBOLS,dateSymbols);
		}
		
		calendar = (Calendar)ThreadLocalMap.get(StaticString.CALENDAR);
		if (calendar == null) {
			calendar = Calendar.getInstance();
			ThreadLocalMap.put(StaticString.CALENDAR,calendar);
		}
	}
}
