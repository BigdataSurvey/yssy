package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class SgBetRecord extends BaseBean{
	
	private Long id;
	
	private Long userId;
	
	private String orderNo;
	
	private int periodsNum;

	private String betIndex;
	
	private JSONObject betInfo;

	private JSONObject settleInfo;
	
	private BigDecimal betAmount;
	
	private BigDecimal profit;
	

	private Integer winOrLose;
	
	private Integer status;

	private String lotteryResult;
	
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

	public JSONObject getBetInfo() {
		return betInfo;
	}

	public void setBetInfo(JSONObject betInfo) {
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

	public JSONObject getSettleInfo() {
		return settleInfo;
	}

	public void setSettleInfo(JSONObject settleInfo) {
		this.settleInfo = settleInfo;
	}

	public void setLotteryResult(String lotteryResult) {
		this.lotteryResult = lotteryResult;
	}

	public String getBetIndex() {
		return betIndex;
	}

	public void setBetIndex(String betIndex) {
		this.betIndex = betIndex;
	}
}
