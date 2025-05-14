package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class Question extends BaseBean {

	//支持富文本
	public static final int CHAT = 1;
	
	public static final int URL = 2;
	
	public static final int GO_PAGE = 3;
	
	public static final int REPLY = 4; //需要服务端回复
	
	private String id;
	
	private String title;
	
	private Integer type;
	
	private String result;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public static int getUrl() {
		return URL;
	}
	
	
}
