package com.zywl.app.defaultx.cache;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zywl.app.base.bean.Achievement;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.AchievementService;


@Service
public class AchievementCacheService extends RedisService{

	
	@Autowired
	private AchievementService achievementService;
	
	
	public List<Achievement> getAllAchievement(){
		String key = RedisKeyConstant.APP_ACHIEVEMENT_LIST;
		List<Achievement> achievements = getList(key,Achievement.class);
		if (achievements==null || achievements.size()==0) {
			achievements = achievementService.findAllAchievement();
			if (achievements!=null) {
				set(key, achievements);
			}
			
		}
		return achievements;
		
	}
	
	public List<Achievement> getAchievementsByType(int type){
		String key = RedisKeyConstant.APP_ACHIEVEMENT_TYPE_LIST+type+"-";
		List<Achievement> achievements = getList(key, Achievement.class);
		if (achievements==null || achievements.size()==0) {
			achievements = achievementService.findAchievementByType(type);
			if (achievements!=null) {
				set(key, achievements);
			}
			
		}
		return achievements;
	}
	
	public Achievement getAchievementById(String id) {
		List<Achievement> achievements = getAllAchievement();
		Achievement achievement = null;
		for (Achievement obj : achievements) {
			if (obj.getId().toString().equals(id)) {
				achievement = obj;
				break;
			}
		}
		return achievement;
	}
	
	public void removeAchievementInfo(Integer type) {
		del(RedisKeyConstant.APP_ACHIEVEMENT_LIST);
		if (type!=null) {
			del(RedisKeyConstant.APP_ACHIEVEMENT_TYPE_LIST+type+"-");
		}
		
	}
	
}
