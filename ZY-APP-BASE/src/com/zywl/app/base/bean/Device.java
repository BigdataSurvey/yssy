package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class Device extends BaseBean {

	public final static int IOS = 1;
	
	public final static int ANDROD = 2;

	private String id;
	
	private Integer deviceType;
	
	private String sourceId;
	
	private String brand;
	
	private Long activeTime;
	
	private Long lastActiveTime;
	
	private String versionNum;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Long getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(Long activeTime) {
		this.activeTime = activeTime;
	}

	public Long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(Long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public String getVersionNum() {
		return versionNum;
	}

	public void setVersionNum(String versionNum) {
		this.versionNum = versionNum;
	}

}
