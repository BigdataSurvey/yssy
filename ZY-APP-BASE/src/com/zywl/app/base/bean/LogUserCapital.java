package com.zywl.app.base.bean;


import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资产明细表
 * 
 * @author Clark
 * @date 2023-03-21 20:57:03
 */
public class LogUserCapital extends BaseBean{
	
	public final static String tablePrefix = "log_user_capital_";
	
	public final static String dateFormat = "yyyy_MM";

	/**
	 * 主键ID
	 */
	private Long id;
	/**
	 * 日志变更类型
	 */
	private Integer logType;
	
	/**
	 * 用户ID
	 */
	private Long userId;
	/**
	 * 资产类型
	 */
	private String capitalType;
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
	 * 占用金额：变更前
	 */
	private BigDecimal occupyBalanceBefore;
	/**
	 * 占用金额：变更后
	 */
	private BigDecimal occupyBalanceAfter;
	/**
	 * 备注
	 */
	private String mark;
	/**
	 * 关联主表表名
	 */
	private String sourceTableName;
	/**
	 * 关联主表主键ID
	 */
	private Long sourceDataId;
	/**
	 * 关联订单号
	 */
	private String sourceNo;
	/**
	 * 来源
	 */
	private String sourceType;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getLogType() {
		return logType;
	}
	public void setLogType(Integer logType) {
		this.logType = logType;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getCapitalType() {
		return capitalType;
	}
	public void setCapitalType(String capitalType) {
		this.capitalType = capitalType;
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
	public BigDecimal getOccupyBalanceBefore() {
		return occupyBalanceBefore;
	}
	public void setOccupyBalanceBefore(BigDecimal occupyBalanceBefore) {
		this.occupyBalanceBefore = occupyBalanceBefore;
	}
	public BigDecimal getOccupyBalanceAfter() {
		return occupyBalanceAfter;
	}
	public void setOccupyBalanceAfter(BigDecimal occupyBalanceAfter) {
		this.occupyBalanceAfter = occupyBalanceAfter;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getSourceTableName() {
		return sourceTableName;
	}
	public void setSourceTableName(String sourceTableName) {
		this.sourceTableName = sourceTableName;
	}
	public Long getSourceDataId() {
		return sourceDataId;
	}
	public void setSourceDataId(Long sourceDataId) {
		this.sourceDataId = sourceDataId;
	}
	public String getSourceNo() {
		return sourceNo;
	}
	public void setSourceNo(String sourceNo) {
		this.sourceNo = sourceNo;
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
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	
	
	
	

}
