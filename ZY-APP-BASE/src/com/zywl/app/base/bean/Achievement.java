package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class Achievement extends BaseBean{
	
	private Long id;
	
	private String context;
	
	private String condition;

	private String category;

	private int expand;

	private int group;

	private int groupId;


	
	private JSONArray reward;
	
	private Integer sort;
	

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

	public JSONArray getReward() {
		return reward;
	}

	public void setReward(JSONArray reward) {
		this.reward = reward;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
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
}
