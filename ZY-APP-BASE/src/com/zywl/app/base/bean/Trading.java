package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class Trading extends BaseBean{
	
	private Long id;
	
	private Long userId;
	
	private Long itemId;
	
	private Integer itemType;
	
	private Integer itemNumber;
	
	private Integer itemAllNumber;
	
	private BigDecimal itemPrice;
	
	private Integer type;
	
	private Integer status;
	
	private Date createTime;
	
	private Date updateTime;

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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

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

	public Integer getItemType() {
		return itemType;
	}

	public void setItemType(Integer itemType) {
		this.itemType = itemType;
	}

	
	public Integer getItemAllNumber() {
		return itemAllNumber;
	}

	public void setItemAllNumber(Integer itemAllNumber) {
		this.itemAllNumber = itemAllNumber;
	}

	@Override
	public String toString() {
		return "Trading [id=" + id + ", userId=" + userId + ", itemId=" + itemId + ", itemType=" + itemType
				+ ", itemNumber=" + itemNumber + ", itemPrice=" + itemPrice + ", type=" + type + ", status=" + status
				+ ", createTime=" + createTime + ", updateTime=" + updateTime + "]";
	}
	
	

}
