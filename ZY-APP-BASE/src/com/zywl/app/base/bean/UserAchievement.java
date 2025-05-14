package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class UserAchievement extends BaseBean{
	
	private Long id;
	
	private Long userId;
	
	private JSONArray achievementList;

	public JSONArray getAchievementList() {
		return achievementList;
	}

	public void setAchievementList(JSONArray achievementList) {
		this.achievementList = achievementList;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}


	
	

}
