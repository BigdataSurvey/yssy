package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class Template extends BaseBean {
	private String id;
	
	private String name;
	
	private String context;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}
