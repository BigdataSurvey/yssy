package com.zywl.app.base.bean.vo;

public class UserAchievementVo {
	
	private Long id;
	
	private Long achievementId;

	private String context;
	
	private Integer sort;
	
	private String reward;
	
	
	//状态  0: 未完成 1：已完成未领取  2：已完成已领取
	private Integer status;

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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public Long getAchievementId() {
		return achievementId;
	}

	public void setAchievementId(Long achievementId) {
		this.achievementId = achievementId;
	}
}
