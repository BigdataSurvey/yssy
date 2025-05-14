package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserDispatchRecord extends BaseBean{
	
	private Long id;
	
	private Long userId;
	
	private Long dispatchId;

	private String cardList;

	private String orderNo;
	
	private Integer status;
	
	private Date beginTime;
	
	private Date endTime;
	
	

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

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

	public Long getDispatchId() {
		return dispatchId;
	}

	public void setDispatchId(Long dispatchId) {
		this.dispatchId = dispatchId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getCardList() {
		return cardList;
	}

	public void setCardList(String cardList) {
		this.cardList = cardList;
	}
}
