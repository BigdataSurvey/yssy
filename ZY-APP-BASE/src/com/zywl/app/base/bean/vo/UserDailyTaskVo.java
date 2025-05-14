package com.zywl.app.base.bean.vo;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class UserDailyTaskVo extends BaseBean {
	
	private Long id;

	private int condition;
	
	private String context;

	private long schedule;
	
	private Integer sort;
	
	private JSONArray reward;
	
	
	//状态  0: 未完成 1：已完成未领取  2：已完成已领取
	private int status;

	private int type;

	public long getSchedule() {
		return schedule;
	}

	public void setSchedule(long schedule) {
		this.schedule = schedule;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public JSONArray getReward() {
		return reward;
	}

	public void setReward(JSONArray reward) {
		this.reward = reward;
	}

	public int getStatus() {
		return status;
	}

}
