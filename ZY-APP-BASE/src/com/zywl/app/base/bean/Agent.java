package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class Agent extends BaseBean {

	private String id;
	
	private String parentId;
	
	private String name;
	
	private String username;
	
	private String password;
	
	private Integer downloadNum;
	
	private Integer registNum;
	
	private Integer paymentNum;
	
	private Date createTime;

	public String getId() {
		return id;
	}

	public String getParentId() {
		return parentId;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Integer getDownloadNum() {
		return downloadNum;
	}

	public Integer getRegistNum() {
		return registNum;
	}

	public Integer getPaymentNum() {
		return paymentNum;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDownloadNum(Integer downloadNum) {
		this.downloadNum = downloadNum;
	}

	public void setRegistNum(Integer registNum) {
		this.registNum = registNum;
	}

	public void setPaymentNum(Integer paymentNum) {
		this.paymentNum = paymentNum;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
