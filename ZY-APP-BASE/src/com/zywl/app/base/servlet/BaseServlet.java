package com.zywl.app.base.servlet;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public abstract class BaseServlet extends HttpServlet{

	public static SimplePropertyPreFilter filter = new SimplePropertyPreFilter();

	protected final Log logger = LogFactory.getLog(getClass());

	protected static ExecutorService asyncServletExecutor;
	
	static{
		PropertiesUtil propertiesUtil = new PropertiesUtil("thread.properties");
		asyncServletExecutor = Executors.newFixedThreadPool(propertiesUtil.getInteger("thread.asyncServlet.pool"), new AppDefaultThreadFactory("AsyncServlet"));
	}
	
	public BaseServlet(){}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		String ipAddress = (String)request.getSession().getAttribute("ip");
		logger().debug("["+getRequestUID(request)+"]来自" + ipAddress + "的请求：" + JSON.toJSONString(request.getParameterMap()));
		
		String webResponse = "";
		try{
			Object doProcess = doProcess(request, response, ipAddress);
			if(doProcess instanceof JSONObject){
				webResponse = ((JSONObject)doProcess).toJSONString();
			}else if(doProcess instanceof JSONArray){
				webResponse = ((JSONArray)doProcess).toJSONString();
			}else if(doProcess instanceof AsyncServletProcessor){
				asyncServletExecutor.execute((AsyncServletProcessor)doProcess);
				return;
			}else{
				if(doProcess == null){
					doProcess = "";
				}
				webResponse = String.valueOf(doProcess);
			}
		}catch(AppException e){
			logger().warn("执行异常：" + e);
			webResponse = e.getMessage();
		}catch(Exception e){
			logger().error("未知异常", e);
			webResponse = "服务端异常";
		}
		Response.doResponse(request, response, webResponse);
	}
	
	protected synchronized static String getRequestUID(HttpServletRequest request) {
		if(request == null){
			return null;
		}
		String uid = (String)request.getAttribute("__UID");
		if(uid == null){
			request.setAttribute("__UID", uid = UID.create(4));
		}
		return uid;
	}
	
	/**
	 * 判断对象是否为空
	 * @author Doe.
	 * @param obj
	 * @return
	 */
	protected static boolean isNotNull(Object obj) {
		return obj != null && !obj.equals("");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getParameterMap(HttpServletRequest request) {
	    // 参数Map
	    Map properties = request.getParameterMap();
	    // 返回值Map
	    Map returnMap = new HashMap();
	    Iterator entries = properties.entrySet().iterator();
	    Map.Entry entry;
	    String name = "";
	    String value = "";
	    while (entries.hasNext()) {
	        entry = (Map.Entry) entries.next();
	        name = (String) entry.getKey();
	        Object valueObj = entry.getValue();
	        if(null == valueObj){
	            value = "";
	        }else if(valueObj instanceof String[]){
	            String[] values = (String[])valueObj;
	            for(int i=0;i<values.length;i++){
	                value = values[i] + ",";
	            }
	            value = value.substring(0, value.length()-1);
	        }else{
	            value = valueObj.toString();
	        }
	        returnMap.put(name, value);
	    }
	    return returnMap;
	}
	

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
	
	protected String getRequestContent(ServletInputStream inputStream) {
		if (inputStream == null) {
			return "";
		}
		String str = "";
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
			    sb.append(line);
			}
			str = sb.toString();
		} catch (IOException e) {
			return str;
		}
		return str;
	}
	
	public abstract Object doProcess(HttpServletRequest request, HttpServletResponse response ,String clientIp) throws AppException, Exception;

	protected Log logger() {
		return logger;
	}

}
