package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class MailVo extends BaseBean{
	
	private Long id;
	
	private Long fromUserId;


	private String fromUserNo;

	private String fromUserName;

	private String fromUserHeadImg;
	private Long toUserId;

	private String toUserNo;

	private String toUserName;

	private String toUserHeadImg;
	
	private String title;
	
	private String context;
	
	private Integer isAttachments;
	
	private String attachmentsDetails;
	
	private Integer isRead;
	
	private Integer type;
	
	private Date sendTime;
	
	
	
	
	

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(Long fromUserId) {
		this.fromUserId = fromUserId;
	}

	public Integer getIsRead() {
		return isRead;
	}

	public void setIsRead(Integer isRead) {
		this.isRead = isRead;
	}

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


	public String getAttachmentsDetails() {
		return attachmentsDetails;
	}

	public void setAttachmentsDetails(String attachmentsDetails) {
		this.attachmentsDetails = attachmentsDetails;
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

	public Long getToUserId() {
		return toUserId;
	}

	public void setToUserId(Long toUserId) {
		this.toUserId = toUserId;
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
}
