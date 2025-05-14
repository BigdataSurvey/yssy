package com.live.app.ws.bean;

import com.zywl.app.base.BaseBean;

public class ConnectedData extends BaseBean {
	private String id;
	
	private Object responseShakeHandsData;

	public ConnectedData(String id){
		this.id = id;
	}
	
	public ConnectedData(String id, Object responseShakeHandsData){
		this.id = id;
		this.responseShakeHandsData = responseShakeHandsData;
	}
	
	public String getId() {
		return id;
	}

	public Object getResponseShakeHandsData() {
		return responseShakeHandsData;
	}

	public void setResponseShakeHandsData(Object responseShakeHandsData) {
		this.responseShakeHandsData = responseShakeHandsData;
	}
}
