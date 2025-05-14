package com.live.app.ws.bean;

import com.zywl.app.base.BaseBean;

public class AppCommand extends BaseBean{
	
	
	private String code;
	
	
	
	private Object data;
	
	private String message;
	
	private boolean success;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	

}
