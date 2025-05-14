package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class News extends BaseBean {

	//系统消息
	public final static Integer TYPE_SYSTEM_NEWS = 1;
	
	//直播间消息
	public final static Integer TYPE_LIVE_NEWS = 2;
	
	//隐藏
	public final static Integer DISPLAY_HIDDEN = 0;
	
	//显示
	public final static Integer DISPLAY_SHOW = 1;
	
	//允许回复
	public final static Integer READ_ONLY_NOT = 1;
	
	//不允许回复
	public final static Integer READ_ONLY_YES = 2;
	
	private String id;
	
	private String title;
	
	private String context;
	
	private Integer type;
	
	private String tags;
	
	private Integer readOnly;
	
	private Integer index;
	
	private Integer display;
	
	private Date releaseTime;
	
	private Date createTime;
	
	private String createUserId;
	
	private Integer viewNum;

	private String webPath;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Integer getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Integer readOnly) {
		this.readOnly = readOnly;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getDisplay() {
		return display;
	}

	public void setDisplay(Integer display) {
		this.display = display;
	}

	public Date getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(Date releaseTime) {
		this.releaseTime = releaseTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public Integer getViewNum() {
		return viewNum;
	}

	public void setViewNum(Integer viewNum) {
		this.viewNum = viewNum;
	}

	public String getWebPath() {
		return webPath;
	}

	public void setWebPath(String webPath) {
		this.webPath = webPath;
	}
}
