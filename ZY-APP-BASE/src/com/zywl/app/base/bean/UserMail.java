package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

public class UserMail extends BaseBean{
	
	private Long userId;
	
	private JSONArray readMailList;

	private JSONArray deleteMailList;

	public JSONArray getDeleteMailList() {
		return deleteMailList;
	}

	public void setDeleteMailList(JSONArray deleteMailList) {
		this.deleteMailList = deleteMailList;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public JSONArray getReadMailList() {
		return readMailList;
	}

	public void setReadMailList(JSONArray readMailList) {
		this.readMailList = readMailList;
	}

	
	
	
	

}
