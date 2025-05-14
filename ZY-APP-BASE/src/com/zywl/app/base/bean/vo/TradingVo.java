package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class TradingVo extends BaseBean{
	
	private Long id;
	
	private Long userId;
	
	private Long itemId;
	
	private Integer itemNumber;
	
	private Integer itemAllNumber;
	
	private BigDecimal itemPrice;
	
	private Integer status;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Integer getItemNumber() {
		return itemNumber;
	}

	public void setItemNumber(Integer itemNumber) {
		this.itemNumber = itemNumber;
	}

	public BigDecimal getItemPrice() {
		return itemPrice;
	}

	public void setItemPrice(BigDecimal itemPrice) {
		this.itemPrice = itemPrice;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	
	public Integer getItemAllNumber() {
		return itemAllNumber;
	}

	public void setItemAllNumber(Integer itemAllNumber) {
		this.itemAllNumber = itemAllNumber;
	}

	@Override
	public String toString() {
		return "TradingVo [id=" + id + ", userId=" + userId + ", itemId=" + itemId + ", itemNumber=" + itemNumber
				+ ", itemPrice=" + itemPrice + ", status=" + status + "]";
	}
	
	
	

}
