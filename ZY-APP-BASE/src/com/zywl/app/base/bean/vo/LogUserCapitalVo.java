package com.zywl.app.base.bean.vo;


import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资产明细表
 * 
 * @author Clark
 * @date 2023-03-21 20:57:03
 */
public class LogUserCapitalVo extends BaseBean{

	/**
	 * 用户ID
	 */
	private Long userId;
	
	/**
	 * 变更金额
	 */
	private BigDecimal amount;
	/**
	 * 余额：变更前
	 */
	private BigDecimal balanceBefore;
	/**
	 * 余额：变更后
	 */
	private BigDecimal balanceAfter;
	
	
	/**
	 * 来源
	 */
	private String sourceType;
	/**
	 * 创建时间
	 */
	private Date createTime;
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
	public BigDecimal getBalanceBefore() {
		return balanceBefore;
	}
	public void setBalanceBefore(BigDecimal balanceBefore) {
		this.balanceBefore = balanceBefore;
	}
	public BigDecimal getBalanceAfter() {
		return balanceAfter;
	}
	public void setBalanceAfter(BigDecimal balanceAfter) {
		this.balanceAfter = balanceAfter;
	}
	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
	
	
	
	
	

}
