package com.zywl.app.base;

import com.zywl.app.base.exp.AppException;
import org.apache.commons.logging.Log;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Base {

	/**
	 * 判断对象是否为空
	 * @author Doe.
	 * @param obj
	 * @return
	 */
	protected static boolean isNotNull(Object ...object) {
		for (Object obj : object) {
			if(obj == null || obj.equals("")){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @author Doe.
	 * @param obj
	 * @return
	 */
	protected static boolean isNull(Object obj) {
		
		return !isNotNull(obj);
	}
	
	/**
	 * 不是空Map
	 * @param map
	 * @return
	 */
	protected boolean isNotEmptyMap(Map<String, ?> map){
		if(null != map && !map.isEmpty()){
			return true;
		}
		return false;
	}
	
	/**
	 * Object转化String
	 * @author Doe.
	 * @param object
	 * @return
	 */
	protected static String valueOf(Object object){
		
		return object == null ? null : String.valueOf(object);
	}
	
	public static boolean eq(Object o1 ,Object o2) {
		if(o1 == o2)
			return true;
		else if(o1 != null && o2 != null)
			return o1.equals(o2);
		return false;
	}

	protected static void throwExp(String message){
		throw new AppException(message);
	}
	
	protected static void checkNull(Object ...objs){
		String exp = "参数异常";
		if(objs == null){
			throwExp(exp);
		}
		if(eq(objs[0], "en_US")){
			exp = "Parameter Exception";
		}
		for (Object object : objs) {
			if(isNull(object))
				throwExp(exp);
		}
	}
	
	protected static boolean regEx(String regEx, String str) {
		Pattern pattern = Pattern.compile(regEx);
	    Matcher matcher = pattern.matcher(str);
	    return matcher.matches();
	}
	
	protected static BigDecimal getBigDecimal(BigDecimal bigDecimal) {
		if(isNull(bigDecimal)){
			return BigDecimal.ZERO;
		}
		return bigDecimal;
	}
	
	protected abstract Log logger();
}
