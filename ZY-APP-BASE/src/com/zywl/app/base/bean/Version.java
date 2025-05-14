package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class Version extends BaseBean {

	public final static int RELEASE_ENABLE = 1; //发布
	
	public final static int RELEASE_DISABLE = 0; //停止发布
	
	public final static int FC_UPDATE = 1; //强制更新
	
	public final static int NORMAL_UPDATE = 0; //常规更新
	
	public final static int TYPE_IOS = 1;
	
	public final static int TYPE_ANDROID = 2;
	
	public final static String APP_IOS = "ipa";
	
	public final static String APP_ANDROID = "apk";
	
	public Version(){}
	
	public Version(String id) {
		this.id = id;
	}

	private String id;
	
	private String description;
	
	private String versionName;
	
	private Integer versionNo;
	
	private String downloadUrl;
	
	private String updateUrl;
	
	private Integer fc;
	
	private String updateTime;
	
	private Integer release;
	
	private Integer type;
	
	
	
	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Integer getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
	}

	public Integer getFc() {
		return fc;
	}

	public void setFc(Integer fc) {
		this.fc = fc;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getRelease() {
		return release;
	}

	public void setRelease(Integer release) {
		this.release = release;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
