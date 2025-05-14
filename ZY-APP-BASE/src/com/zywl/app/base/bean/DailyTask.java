package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class DailyTask extends BaseBean{
	
	private Long id;
	
	private String context;
	
	private Integer condition;

	private String category;

	private int expand;


	
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

	public Integer getCondition() {
		return condition;
	}

	public void setCondition(Integer condition) {
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
}
