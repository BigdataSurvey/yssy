package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;
import com.zywl.app.base.util.DateUtil;

import java.math.BigDecimal;
import java.util.Date;

public class UserCapital extends BaseBean{
	
	public static final  String tablePrefix = "t_user_capital";

	private Long id;
	
	private Long userId;
	
	private Integer capitalType;

	private BigDecimal occupyBalance;
	
	private BigDecimal Balance;
	
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

	public Integer getCapitalType() {
		return capitalType;
	}

	public void setCapitalType(Integer capitalType) {
		this.capitalType = capitalType;
	}

	public BigDecimal getOccupyBalance() {
		return occupyBalance;
	}

	public void setOccupyBalance(BigDecimal occupyBalance) {
		this.occupyBalance = occupyBalance;
	}

	public BigDecimal getBalance() {
		return Balance;
	}

	public void setBalance(BigDecimal balance) {
		Balance = balance;
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

	@Override
	public String toString() {
		return "UserCapital [id=" + id + ", userId=" + userId + ", capitalType=" + capitalType + ", occupyBalance="
				+ occupyBalance + ", Balance=" + Balance + ", createTime=" + DateUtil.format0(createTime)+ ", updateTime=" + DateUtil.format0(updateTime)
				+ "]";
	}
	
	
	
	
	
}
