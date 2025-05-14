package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class TaskOrder extends BaseBean{
	
	private Long id;
	
	private String orderNo;
	
	private String sql;
	
	private String server;
	
	private int type;
	
	private int status;
	
	private Long userId;
	
	private BigDecimal amount;
	
	private int capitalType;
	
	private Long activeTime;
	
	private Date createTime;
	
	private Date updateTime;
	
	

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	

	public Long getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(Long activeTime) {
		this.activeTime = activeTime;
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

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCapitalType() {
		return capitalType;
	}

	public void setCapitalType(int capitalType) {
		this.capitalType = capitalType;
	}
	
	

}
