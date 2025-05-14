package com.zywl.app.defaultx.cache;

import org.springframework.stereotype.Service;

import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.impl.RedisService;

@Service
public class CodeCacheService extends RedisService{
	
	
	
	public void saveCode(String verify,String code) {
		String key = RedisKeyConstant.APP_VERIFY_CODE+verify;
		set(key, code, 300L);
	}
	
	public String getCode(String verify) {
		String key = RedisKeyConstant.APP_VERIFY_CODE+verify;
		return get(key);
	}
	
	public void removeCode(String verify) {
		del(RedisKeyConstant.APP_VERIFY_CODE+verify);
	}

}
