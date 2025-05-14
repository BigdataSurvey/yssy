package com.zywl.app.manager.bean;

import com.zywl.app.base.BaseBean;

public class ServerTaskBean extends BaseBean {
	
	private int task;
	
	private int qps;
	
	private String datetime;

	public ServerTaskBean(){}
	
	public ServerTaskBean(int task, int qps, String datetime) {
		this.task = task;
		this.qps = qps;
		this.datetime = datetime;
	}

	public int getTask() {
		return task;
	}

	public void setTask(int task) {
		this.task = task;
	}

	public int getQps() {
		return qps;
	}

	public void setQps(int qps) {
		this.qps = qps;
	}

	public String getDatetime() {
		return datetime;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
}
