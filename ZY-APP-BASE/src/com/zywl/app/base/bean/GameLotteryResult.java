package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class GameLotteryResult extends BaseBean{
	
	private Long id;
	
	private Long gameId;
	
	private String periodsNum;
	
	private String lotteryResult;
	
	private BigDecimal playerBet;
	
	private BigDecimal playerProfit;
	
	private BigDecimal winLose;
	
	private Integer allTakeNum;
	
	private Integer winNum;
	
	private Integer loseNum;
	
	private Integer status;
	
	private Date createTime;
	
	

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

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public String getPeriodsNum() {
		return periodsNum;
	}

	public void setPeriodsNum(String periodsNum) {
		this.periodsNum = periodsNum;
	}

	public String getLotteryResult() {
		return lotteryResult;
	}

	public void setLotteryResult(String lotteryResult) {
		this.lotteryResult = lotteryResult;
	}

	public BigDecimal getPlayerBet() {
		return playerBet;
	}

	public void setPlayerBet(BigDecimal playerBet) {
		this.playerBet = playerBet;
	}

	public BigDecimal getPlayerProfit() {
		return playerProfit;
	}

	public void setPlayerProfit(BigDecimal playerProfit) {
		this.playerProfit = playerProfit;
	}

	public BigDecimal getWinLose() {
		return winLose;
	}

	public void setWinLose(BigDecimal winLose) {
		this.winLose = winLose;
	}

	public Integer getAllTakeNum() {
		return allTakeNum;
	}

	public void setAllTakeNum(Integer allTakeNum) {
		this.allTakeNum = allTakeNum;
	}

	public Integer getWinNum() {
		return winNum;
	}

	public void setWinNum(Integer winNum) {
		this.winNum = winNum;
	}

	public Integer getLoseNum() {
		return loseNum;
	}

	public void setLoseNum(Integer loseNum) {
		this.loseNum = loseNum;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	

}
