package com.live.app.ws.bean;

import com.live.app.ws.enums.PushCode;
import com.zywl.app.base.BaseBean;

public class PushBean extends BaseBean {

	private PushCode pushCode;

	private String condition;

	private Object shakeHands;

	public PushBean() {
	}

	public PushBean(PushCode pushCode) {
		super();
		this.pushCode = pushCode;
	}

	public PushBean(PushCode pushCode, String condition) {
		super();
		this.pushCode = pushCode;
		this.condition = condition;
	}

	public PushBean(PushCode pushCode, String condition, Object shakeHands) {
		super();
		this.pushCode = pushCode;
		this.condition = condition;
		this.shakeHands = shakeHands;
	}

	public PushCode getPushCode() {
		return pushCode;
	}

	public void setPushCode(PushCode pushCode) {
		this.pushCode = pushCode;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public Object getShakeHands() {
		return shakeHands;
	}

	public void setShakeHands(Object shakeHands) {
		this.shakeHands = shakeHands;
	}
}
