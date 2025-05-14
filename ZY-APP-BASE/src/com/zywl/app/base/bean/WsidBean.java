package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

public class WsidBean extends BaseBean {

	public final static long TIMEOUT = 604800000L;
	
	private String wsId;
	
	private Long userId;
	
	private String versionId;
	
	private String authServerAddress;

	private String authServerHost;
	
	private Long timeout;
	
	private String wsPrivateKey;
	
	private JSONObject attr = new JSONObject();
	
	
	
	public String getWsid() {
		return wsId;
	}

	public void setWsid(String wsid) {
		this.wsId = wsid;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public String getAuthServerAddress() {
		return authServerAddress;
	}

	public void setAuthServerAddress(String authServerAddress) {
		this.authServerAddress = authServerAddress;
	}

	public String getAuthServerHost() {
		return authServerHost;
	}

	public void setAuthServerHost(String authServerHost) {
		this.authServerHost = authServerHost;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public String getWsPrivateKey() {
		return wsPrivateKey;
	}

	public void setWsPrivateKey(String wsPrivateKey) {
		this.wsPrivateKey = wsPrivateKey;
	}

	public JSONObject getAttr() {
		return attr;
	}

	public void setAttr(JSONObject attr) {
		this.attr = attr;
	}

	
	
	
}
