package com.zywl.app.base.bean.vo;

import java.math.BigDecimal;

public class UserCapitalVo {
	
	private Integer capitalType;
	
	private BigDecimal Balance;

	public Integer getCapitalType() {
		return capitalType;
	}

	public void setCapitalType(Integer capitalType) {
		this.capitalType = capitalType;
	}

	public BigDecimal getBalance() {
		return Balance;
	}

	public void setBalance(BigDecimal balance) {
		Balance = balance;
	}
	
	

}
