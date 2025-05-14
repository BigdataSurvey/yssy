package com.zywl.app.defaultx.bean;

import com.zywl.app.base.BaseBean;

public class SessionBean extends BaseBean {
	private String wsid;
	
	private String sessionId;
	
	private String ip;
	
	private String lastConnectTime;
	
	private String lastLoginTime;
	
	private String userId;
	
	private String phone;
	
	private String name;
	
	private String login;

	public String getWsid() {
		return wsid;
	}

	public void setWsid(String wsid) {
		this.wsid = wsid;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLastConnectTime() {
		return lastConnectTime;
	}

	public void setLastConnectTime(String lastConnectTime) {
		this.lastConnectTime = lastConnectTime;
	}

	public String getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
}
