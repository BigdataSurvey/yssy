package com.zywl.app.defaultx.cache;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.LogSign;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.LogSignService;

@Service
public class UserSignCacheService extends RedisService{
	
	
	
	
	@Autowired
	private  LogSignService logSignService;
	
	public List<LogSign> getUserSignLog(Long userId){
		String key = RedisKeyConstant.APP_USER_SIGN+DateUtil.getMonthValue()+":"+userId+"-";
		String a = get(key);
		List<LogSign> logs = JSON.parseArray(a, LogSign.class); 
		if (logs==null) {
			logs = logSignService.findLogSignByUserId(userId);
			if (logs!=null && logs.size()!=0) {
				set(key, JSONObject.toJSONString(logs),86400L*31*2);
			}
		
		}
		return logs==null? new ArrayList<LogSign>():logs;
	}
	
	
	public void addLogSignCache(Long userId,LogSign logSign) {
		List<LogSign> logs = getUserSignLog(userId);
		logs.add(logSign);
		String key = RedisKeyConstant.APP_USER_SIGN+DateUtil.getMonthValue()+":"+userId+"-";
		set(key, JSONObject.toJSONString(logs),86400L*31);
	}
	
	public void removeCache(Long userId) {
		String key = RedisKeyConstant.APP_USER_SIGN+DateUtil.getMonthValue()+":"+userId+"-";
		del(key);
	}
	

}
