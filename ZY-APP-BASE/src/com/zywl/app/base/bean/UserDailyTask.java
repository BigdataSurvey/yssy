package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.io.Serializable;
import java.util.Date;




public class UserDailyTask extends BaseBean {

	//
	private Long id;
	//
	private Long userId;
	//
	private String ymd;
	//
	private JSONArray taskList;

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

	/**
	 * 设置：
	 */
	public void setYmd(String ymd) {
		this.ymd = ymd;
	}
	/**
	 * 获取：
	 */
	public String getYmd() {
		return ymd;
	}

	public JSONArray getTaskList() {
		return taskList;
	}

	public void setTaskList(JSONArray taskList) {
		this.taskList = taskList;
	}
}
