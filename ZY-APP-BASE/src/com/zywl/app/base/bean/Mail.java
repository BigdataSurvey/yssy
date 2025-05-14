package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.util.Date;

public class Mail extends BaseBean{
	
	private Long id;
	
	private Long fromUserId;

	private String fromUserNo;

	private String fromUserName;

	private int fromUserRoleId;

	private String fromUserHeadImg;
	private Long toUserId;

	private String toUserNo;

	private int toUserRoleId;

	private String toUserName;

	private String toUserHeadImg;
	private String title;
	
	private String context;
	
	private Integer isAttachments;
	
	private JSONArray attachmentsDetails;
	
	private Integer userGroup;
	
	private Integer type;

	private Integer isRead;
	
	private Integer status;
	
	private Date sendTime;
	
	private Date expirationTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public Integer getIsAttachments() {
		return isAttachments;
	}

	public void setIsAttachments(Integer isAttachments) {
		this.isAttachments = isAttachments;
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


	public JSONArray getAttachmentsDetails() {
		return attachmentsDetails;
	}

	public void setAttachmentsDetails(JSONArray attachmentsDetails) {
		this.attachmentsDetails = attachmentsDetails;
	}

	public Integer getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(Integer userGroup) {
		this.userGroup = userGroup;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Date getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}

	public Long getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(Long fromUserId) {
		this.fromUserId = fromUserId;
	}

	public Long getToUserId() {
		return toUserId;
	}

	public void setToUserId(Long toUserId) {
		this.toUserId = toUserId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getIsRead() {
		return isRead;
	}

	public void setIsRead(Integer isRead) {
		this.isRead = isRead;
	}

	public String getFromUserNo() {
		return fromUserNo;
	}

	public void setFromUserNo(String fromUserNo) {
		this.fromUserNo = fromUserNo;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public String getFromUserHeadImg() {
		return fromUserHeadImg;
	}

	public void setFromUserHeadImg(String fromUserHeadImg) {
		this.fromUserHeadImg = fromUserHeadImg;
	}

	public String getToUserNo() {
		return toUserNo;
	}

	public void setToUserNo(String toUserNo) {
		this.toUserNo = toUserNo;
	}

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public String getToUserHeadImg() {
		return toUserHeadImg;
	}

	public void setToUserHeadImg(String toUserHeadImg) {
		this.toUserHeadImg = toUserHeadImg;
	}

	public int getFromUserRoleId() {
		return fromUserRoleId;
	}

	public void setFromUserRoleId(int fromUserRoleId) {
		this.fromUserRoleId = fromUserRoleId;
	}

	public int getToUserRoleId() {
		return toUserRoleId;
	}

	public void setToUserRoleId(int toUserRoleId) {
		this.toUserRoleId = toUserRoleId;
	}
}
