package com.zywl.app.defaultx.cache;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Backpack;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.BackpackService;

@Service
public class UserBackpackCacheService extends RedisService{
	
	

	
	@PostConstruct
	public void _construct(){
		reloadCache();
	}
	
	public void reloadCache(){
		logger.info("删除用户背包缓存");
		deleteByLikeKey(RedisKeyConstant.APP_USER_BACKPACK+"*");
		logger.info("删除用户背包缓存结束");
	}
	
	@Autowired
	private BackpackService backpackService;

	
	public List<Backpack> getUserBackpackByCache(Long userId){
		String key = RedisKeyConstant.APP_USER_BACKPACK+userId+"-";
		List<Backpack> list = getList(key, Backpack.class);
		if (list==null || list.size()==0) {
			//读取数据库
			list = backpackService.getBackpackByUserId(userId);
			if (list!=null) {
				set(key, list, 1000L);
			}
		}
		return list;
	}
	
	public Map<String, Backpack> getBackpackMap(Long userId){
		List<Backpack> list = getUserBackpackByCache(userId);
		Map<String, Backpack> map = new HashMap<String, Backpack>();
		for (Backpack backpack : list) {
			map.put(backpack.getItemId().toString(), backpack);
		}
		return map;
	}
	
	
	public void reloadBackpackCache(Long userId) {
		getUserBackpackByCache(userId);
	}
	
	public void delUserBackpackCache(Long userId) {
		del(RedisKeyConstant.APP_USER_BACKPACK+userId+"-");
	}
}
