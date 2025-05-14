package com.zywl.app.defaultx.cache;

import org.springframework.stereotype.Service;

import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.impl.RedisService;

@Service
public class LockCacheService extends RedisService{
	
	
	public void beginTask() {
		set(RedisKeyConstant.APP_LOCK, 1,600L);
	}
	
	
	public void endTask() {
		del(RedisKeyConstant.APP_LOCK);
	}
	
	public boolean canTask() {
		return hasKey(RedisKeyConstant.APP_LOCK);
	}

}
