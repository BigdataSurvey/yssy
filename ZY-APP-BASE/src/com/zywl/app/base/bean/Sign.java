package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class Sign extends BaseBean{
	
	private Long id;
	
	private Long userId;
	
	//总天数
	private Integer days;
	
	//周连续签到天数
	private Integer weekDays;
	
	//月连续签到天数
	private Integer monthDays;
	
	//年连续签到天数
	private Integer yearDays;
	
	private Date lastSignTime;
	
	//可补签次数
	private Integer signNums;
	
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getDays() {
		return days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}

	public Integer getWeekDays() {
		return weekDays;
	}

	public void setWeekDays(Integer weekDays) {
		this.weekDays = weekDays;
	}

	public Integer getMonthDays() {
		return monthDays;
	}

	public void setMonthDays(Integer monthDays) {
		this.monthDays = monthDays;
	}

	public Integer getYearDays() {
		return yearDays;
	}

	public void setYearDays(Integer yearDays) {
		this.yearDays = yearDays;
	}

	public Date getLastSignTime() {
		return lastSignTime;
	}

	public void setLastSignTime(Date lastSignTime) {
		this.lastSignTime = lastSignTime;
	}

	public Integer getSignNums() {
		return signNums;
	}

	public void setSignNums(Integer signNums) {
		this.signNums = signNums;
	}
	
	

}
