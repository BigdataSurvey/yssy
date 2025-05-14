package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Config;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ConfigService extends DaoService {
	
	public ConfigService() {
		super("ConfigMapper");
	}
	
	@Transactional
	public void updateConfig(Config config){
		update(config);
	}
	
	
	public Config getConfigByKey(String key) {
		Map<String, String> map = new HashedMap<String, String>();
		map.put("key",key);
		Config config = findOne(key);
		return config;
	}
}
