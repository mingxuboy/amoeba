package com.meidusa.amoeba.mongodb.handler.merge;

import java.math.BigDecimal;

public class MergeMath {
	
	public static Number div(Number param1,Number param2){
		// BigInteger type is not supported
		if (param1 instanceof Double || param2 instanceof Double || param1 instanceof Float || param2 instanceof Float) {
			return ((Number)param1).doubleValue() / ((Number)param2).doubleValue();
		}
		BigDecimal b1 = getBigDecimal((Number)param1);
		BigDecimal b2 = getBigDecimal((Number)param2);
		return b1.divide(b2, 40, BigDecimal.ROUND_HALF_UP);
	}
	
	private static BigDecimal getBigDecimal(Number param) {
		// BigInteger is not supported
		if (param instanceof BigDecimal) {
			return (BigDecimal)param;
		}
		if (param instanceof Double || param instanceof Float) {
			return new BigDecimal(param.doubleValue());
		}
		if(param == null){
			return new BigDecimal(0);
		}else{
			return new BigDecimal(param.longValue());
		}
	}
	
	public static Number add(Number param1,Number param2){
		Number n1 = (Number)param1;
		Number n2 = (Number)param2;
		if (n1 instanceof BigDecimal || n2 instanceof BigDecimal) {
			BigDecimal b1 = getBigDecimal(n1);
			BigDecimal b2 = getBigDecimal(n2);
			return b1.add(b2);
		}
		if (n1 instanceof Double || n2 instanceof Double || n1 instanceof Float || n2 instanceof Float) {
			return n1.doubleValue() + n2.doubleValue();
		} else {	// Long, Integer, Short, Byte 
			long l1 = n1.longValue();
			long l2 = n2.longValue();
			long r = l1 + l2;
			if (l1 <= r && l2 <= r) {		// overflow check
				return r;
			} else {
				BigDecimal b1 = new BigDecimal(l1);
				BigDecimal b2 = new BigDecimal(l2);
				return b1.add(b2);
			}
		}
	}
	
	public static Number mul(Number param1,Number param2){
		// BigInteger type is not supported
		if (param1 instanceof BigDecimal || param2 instanceof BigDecimal) {
			BigDecimal b1 = getBigDecimal((Number)param1);
			BigDecimal b2 = getBigDecimal((Number)param2);
			return b1.multiply(b2);
		}
		if (param1 instanceof Double || param2 instanceof Double || param1 instanceof Float || param2 instanceof Float) {
			return ((Number)param1).doubleValue() * ((Number)param2).doubleValue();
		} else {	// Long, Integer, Short, Byte 
			long l1 = ((Number)param1).longValue();
			long l2 = ((Number)param2).longValue();
			long r = l1 * l2;
			if (l1 <= r && l2 <= r) {		// overflow check
				return r;
			} else {
				BigDecimal b1 = new BigDecimal(l1);
				BigDecimal b2 = new BigDecimal(l2);
				return b1.multiply(b2);
			}
		}
	}
}
