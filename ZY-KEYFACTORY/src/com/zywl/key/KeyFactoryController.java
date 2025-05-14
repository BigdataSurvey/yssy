package com.zywl.key;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.DigestUtils;

import com.alibaba.fastjson2.JSONObject;


import sun.security.provider.MD5;


public class KeyFactoryController extends HttpServlet{
	
	public static ConcurrentMap<String, Map<String, String>> map= new ConcurrentHashMap<String, Map<String,String>>() ;
	
	
	
	public void doGet(HttpServletRequest req ,HttpServletResponse res)throws ServletException,IOException{
		String method = req.getParameter("method");
		String publicKey = req.getParameter("key");
		JSONObject result = new JSONObject();
		
		if ((null == method || null == publicKey || "".equals(method) || "".equals(publicKey)) || ( !"create".equals(method) && !"get".equals(method) ) ) {
			result.put("code", 0);
			result.put("data",null);
			result.put("message", "参数异常");
			res.getWriter().append(result.toJSONString());
			return;
		}
		String privateKey = " ";
		if (method.equals("create")) {
		 	try { 
		 		privateKey = DigestUtils.md5DigestAsHex(publicKey.getBytes("utf-8"));
		 		Map<String, String> timePrivateKey = new HashMap<String, String>();
		 		timePrivateKey.put(System.currentTimeMillis()+"", privateKey);
		 		map.put(publicKey, timePrivateKey);
		    } catch (UnsupportedEncodingException e) { 
		    e.printStackTrace();
		    }
		}else {
			Set<String> set = map.get(publicKey).keySet();
			privateKey = "";
			for(String time:set ) {
				privateKey= map.get(publicKey).get(time);
			}
			map.remove(publicKey);
		}
		result.put("code", 1);
		result.put("message", "");
		result.put("data", privateKey);
		res.getWriter().append(result.toJSONString());
	}
	public void doPost(HttpServletRequest req ,HttpServletResponse res)throws ServletException,IOException{
		String method = req.getParameter("method");
		String publicKey = req.getParameter("key");
		JSONObject result = new JSONObject();
		if (null == method || null == publicKey) {
			result.put("code", 0);
			result.put("data",null);
			result.put("message", "参数异常");
			res.getWriter().append(result.toJSONString());
		}
		String privateKey = " ";
		if (method.equals("create")) {
		 	try { 
		 		privateKey = DigestUtils.md5DigestAsHex(publicKey.getBytes("utf-8"));
		 		Map<String, String> timePrivateKey = new HashMap<String, String>();
		 		timePrivateKey.put(System.currentTimeMillis()+"", privateKey);
		 		map.put(publicKey, timePrivateKey);
		    } catch (UnsupportedEncodingException e) { 
		    e.printStackTrace();
		    }
		}else {
			Set<String> set = map.get(publicKey).keySet();
			privateKey = "";
			for(String time:set ) {
				privateKey= map.get(publicKey).get(time);
			}
			map.remove(publicKey);
		}
		result.put("code", 1);
		result.put("message", "");
		result.put("data", privateKey);
		res.getWriter().append(result.toJSONString());
	}

	
	

}
