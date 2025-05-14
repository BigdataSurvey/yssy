package com.live.app.ws.bean;

import com.zywl.app.base.BaseBean;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.UID;

public class Command extends BaseBean {
	
	private String id = UID.create();
	
	private String code;
	
	private String requestTime = DateUtil.getCurrent0();
	
	/** Server响应时间 */
	private String responseTime;
	
	private Object data;
	
	private String message;
	
	private boolean success=true;
	
	private boolean push;
	
	private String condition;
	
	private String locale;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

	public String getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(String responseTime) {
		this.responseTime = responseTime;
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

	public boolean isPush() {
		return push;
	}

	public void setPush(boolean push) {
		this.push = push;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	
	public Command getCommand(String code,String message) {
		this.code=code;
		this.message=message;
		return this;
	}

}
