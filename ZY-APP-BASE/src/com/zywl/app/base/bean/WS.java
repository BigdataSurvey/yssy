package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class WS extends BaseBean{
	
	private Long userId;
	
	private String wsId;
	
	private String privateKey;
	
	public Date createTime;
	
	public Date updateTime;
	
	

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getWsId() {
		return wsId;
	}

	public void setWsId(String wsId) {
		this.wsId = wsId;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	public WS() {
		
	}
	
	public WS(WsidBean wsBean) {
		this.userId = wsBean.getUserId();
		this.privateKey = wsBean.getWsPrivateKey();
		this.wsId = wsBean.getWsid();
	}

}
