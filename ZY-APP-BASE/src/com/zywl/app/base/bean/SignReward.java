package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class SignReward extends BaseBean{
	
	private Long id;
	
	private Integer month;
	
	private Integer days;
	
	private String context;
	
	private String totalReward;
	
	

	public String getTotalReward() {
		return totalReward;
	}

	public void setTotalReward(String totalReward) {
		this.totalReward = totalReward;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDays() {
		return days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	

}
