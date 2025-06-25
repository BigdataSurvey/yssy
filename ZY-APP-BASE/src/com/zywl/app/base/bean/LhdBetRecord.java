package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class LhdBetRecord extends BaseBean{
	
	private Long id;
	
	private Long userId;
	
	private String orderNo;
	
	private int periodsNum;
	
	private String betInfo;


	private BigDecimal betAmount;
	
	private BigDecimal profit;

	private String lotteryResult;
	

	private Integer winOrLose;
	
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

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public int getPeriodsNum() {
		return periodsNum;
	}

	public void setPeriodsNum(int periodsNum) {
		this.periodsNum = periodsNum;
	}

	public String getBetInfo() {
		return betInfo;
	}

	public void setBetInfo(String betInfo) {
		this.betInfo = betInfo;
	}

	public BigDecimal getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(BigDecimal betAmount) {
		this.betAmount = betAmount;
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}


	public Integer getWinOrLose() {
		return winOrLose;
	}

	public void setWinOrLose(Integer winOrLose) {
		this.winOrLose = winOrLose;
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

	public String getLotteryResult() {
		return lotteryResult;
	}


	public void setLotteryResult(String lotteryResult) {
		this.lotteryResult = lotteryResult;
	}

	@Override
	public String toString() {
		return "LhdBetRecord{" +
				"id=" + id +
				", userId=" + userId +
				", orderNo='" + orderNo + '\'' +
				", periodsNum=" + periodsNum +
				", betInfo='" + betInfo + '\'' +
				", betAmount=" + betAmount +
				", profit=" + profit +
				", lotteryResult='" + lotteryResult + '\'' +
				", winOrLose=" + winOrLose +
				", status=" + status +
				", createTime=" + createTime +
				", updateTime=" + updateTime +
				'}';
	}
}
