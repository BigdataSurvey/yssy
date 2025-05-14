package com.zywl.app.base.bean.vo;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class AchievementVo extends BaseBean{
	
	private Long id;
	
	private String context;

	private int schedule;
	
	private String condition;

	private int expand;

	private int group;

	private int groupId;
	
	private JSONArray reward;

	private Integer type;

	private int status;

	public int getSchedule() {
		return schedule;
	}

	public void setSchedule(int schedule) {
		this.schedule = schedule;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}


	public int getExpand() {
		return expand;
	}

	public void setExpand(int expand) {
		this.expand = expand;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public JSONArray getReward() {
		return reward;
	}

	public void setReward(JSONArray reward) {
		this.reward = reward;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	

}
