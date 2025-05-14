package com.zywl.app.defaultx.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import com.zywl.app.base.bean.Achievement;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class AchievementService extends DaoService {

	public AchievementService() {
		super("AchievementMapper");
	}


	
	public List<Achievement> findAllAchievement() {
		return findAll();
	}
	
	
	
	public List<Achievement> findAchievementByType(int type) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("type", type);
		return findList("findByType", params);
	}
	
	

	
	
}
