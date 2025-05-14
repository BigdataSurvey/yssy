package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class LogSign extends BaseBean{
	
	private Long id;
	
	private Long userId;
	
	private String signContext;
	
	private Date createTime;
	
	private int type;
	
	private int patchDays;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getSignContext() {
		return signContext;
	}

	public void setSignContext(String signContext) {
		this.signContext = signContext;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getPatchDays() {
		return patchDays;
	}

	public void setPatchDays(int patchDays) {
		this.patchDays = patchDays;
	}

	@Override
	public String toString() {
		return "LogSign [id=" + id + ", userId=" + userId + ", signContext=" + signContext + ", createTime="
				+ createTime + ", type=" + type + ", patchDays=" + patchDays + "]";
	}
	
	

}
