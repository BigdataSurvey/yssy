package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class Game extends BaseBean{
	
	private Long id;
	
	private String name;
	
	private Integer status;
	
	private String gameSetting;
	
	private Date createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getGameSetting() {
		return gameSetting;
	}

	public void setGameSetting(String gameSetting) {
		this.gameSetting = gameSetting;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	

}
