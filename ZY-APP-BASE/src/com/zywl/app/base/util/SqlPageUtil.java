package com.zywl.app.base.util;

import com.alibaba.fastjson2.JSONObject;

public class SqlPageUtil {
	
	private final static int MAX_LIMIT = 100;
	
	public static JSONObject getPageLimit(int page, int limit, int maxLimit){
		if(limit > maxLimit) {
			limit = maxLimit;
		}
		
		page = page < 1 ? 1 : page;
		limit = limit < 1 ? 1 : limit;
		
		limit = limit > MAX_LIMIT ? MAX_LIMIT : limit;
		
		JSONObject rs = new JSONObject();
		rs.put("start", (page - 1) * limit);
		rs.put("limit", limit);
		return rs;
	}
	
	public static JSONObject getPageLimit(int page, int limit){
		return getPageLimit(page, limit, Integer.MAX_VALUE);
	}
}
