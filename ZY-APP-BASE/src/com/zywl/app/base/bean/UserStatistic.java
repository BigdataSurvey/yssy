package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
/**
 * 玩家个人统计表
 * **/
public class UserStatistic extends BaseBean{
	
	
	private Long id;
	
	private Long userId;
	
	private BigDecimal acquireRmb;
	
	private BigDecimal cashRmb;
	
	private Integer cashTimes;
	
	private BigDecimal acquireMoney;
	
	private BigDecimal expendMoney;
	
	private Integer advertTimes;
	
	private Integer loginTimes;
	
	private Integer oneJuniorNum;
	
	private Integer twoJuniorNum;

	private BigDecimal createSw;
	
	private BigDecimal createAnima;

	private BigDecimal createAnima2;

	private BigDecimal createGrandfaAnima;

	private BigDecimal createGrandfaAnima2;

	private BigDecimal getAnima;

	private BigDecimal getAnima2;

	private  BigDecimal createIncome;

	private BigDecimal createGrandfaIncome;

	private BigDecimal getIncome;

	private BigDecimal getAllIncome;

	private BigDecimal channelIncome;

	private BigDecimal nowChannelIncome;

	public BigDecimal getCreateGrandfaIncome() {
		return createGrandfaIncome;
	}

	public void setCreateGrandfaIncome(BigDecimal createGrandfaIncome) {
		this.createGrandfaIncome = createGrandfaIncome;
	}

	public BigDecimal getCreateAnima() {
		return createAnima;
	}

	public void setCreateAnima(BigDecimal createAnima) {
		this.createAnima = createAnima;
	}

	public BigDecimal getGetAnima() {
		return getAnima;
	}

	public void setGetAnima(BigDecimal getAnima) {
		this.getAnima = getAnima;
	}

	public BigDecimal getCreateIncome() {
		return createIncome;
	}

	public void setCreateIncome(BigDecimal createIncome) {
		this.createIncome = createIncome;
	}

	public BigDecimal getGetIncome() {
		return getIncome;
	}

	public void setGetIncome(BigDecimal getIncome) {
		this.getIncome = getIncome;
	}

	public Integer getLoginTimes() {
		return loginTimes;
	}

	public void setLoginTimes(Integer loginTimes) {
		this.loginTimes = loginTimes;
	}

	public Integer getOneJuniorNum() {
		return oneJuniorNum;
	}

	public void setOneJuniorNum(Integer oneJuniorNum) {
		this.oneJuniorNum = oneJuniorNum;
	}

	public Integer getTwoJuniorNum() {
		return twoJuniorNum;
	}

	public void setTwoJuniorNum(Integer twoJuniorNum) {
		this.twoJuniorNum = twoJuniorNum;
	}

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

	public BigDecimal getAcquireRmb() {
		return acquireRmb;
	}

	public void setAcquireRmb(BigDecimal acquireRmb) {
		this.acquireRmb = acquireRmb;
	}

	public BigDecimal getCashRmb() {
		return cashRmb;
	}

	public void setCashRmb(BigDecimal cashRmb) {
		this.cashRmb = cashRmb;
	}

	

	public Integer getCashTimes() {
		return cashTimes;
	}

	public void setCashTimes(Integer cashTimes) {
		this.cashTimes = cashTimes;
	}

	public BigDecimal getAcquireMoney() {
		return acquireMoney;
	}

	public void setAcquireMoney(BigDecimal acquireMoney) {
		this.acquireMoney = acquireMoney;
	}

	public BigDecimal getExpendMoney() {
		return expendMoney;
	}

	public void setExpendMoney(BigDecimal expendMoney) {
		this.expendMoney = expendMoney;
	}

	public Integer getAdvertTimes() {
		return advertTimes;
	}

	public void setAdvertTimes(Integer advertTimes) {
		this.advertTimes = advertTimes;
	}

	public BigDecimal getGetAllIncome() {
		return getAllIncome;
	}

	public void setGetAllIncome(BigDecimal getAllIncome) {
		this.getAllIncome = getAllIncome;
	}

	public BigDecimal getChannelIncome() {
		return channelIncome;
	}

	public void setChannelIncome(BigDecimal channelIncome) {
		this.channelIncome = channelIncome;
	}

	public BigDecimal getNowChannelIncome() {
		return nowChannelIncome;
	}

	public BigDecimal getCreateGrandfaAnima() {
		return createGrandfaAnima;
	}

	public void setCreateGrandfaAnima(BigDecimal createGrandfaAnima) {
		this.createGrandfaAnima = createGrandfaAnima;
	}

	public void setNowChannelIncome(BigDecimal nowChannelIncome) {
		this.nowChannelIncome = nowChannelIncome;
	}

	public BigDecimal getCreateAnima2() {
		return createAnima2;
	}

	public void setCreateAnima2(BigDecimal createAnima2) {
		this.createAnima2 = createAnima2;
	}

	public BigDecimal getCreateGrandfaAnima2() {
		return createGrandfaAnima2;
	}

	public void setCreateGrandfaAnima2(BigDecimal createGrandfaAnima2) {
		this.createGrandfaAnima2 = createGrandfaAnima2;
	}

	public BigDecimal getGetAnima2() {
		return getAnima2;
	}

	public void setGetAnima2(BigDecimal getAnima2) {
		this.getAnima2 = getAnima2;
	}

	public BigDecimal getCreateSw() {
		return createSw;
	}

	public void setCreateSw(BigDecimal createSw) {
		this.createSw = createSw;
	}
}
