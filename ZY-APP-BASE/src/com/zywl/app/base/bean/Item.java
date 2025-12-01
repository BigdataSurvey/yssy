package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;
/**
 * 物品道具表
 * **/
public class Item extends BaseBean{
	
	private Long id;
	
	private String name;
	
	private Integer number;
	
	private String context;

	private int isBook;

	private BigDecimal bookReward;
	
	
	//品质
	private Integer quality;

	private Integer isUse;
	
	private Integer isOverlap;
	
	private Integer isSell;

	private Integer isSend;

	private Integer synNumber;

	private Integer canSyn;

	private JSONArray synUse;

	private Integer synRate;

	private String synResultId;
	
	private Integer currencyType;
	
	private BigDecimal price;
	
	private BigDecimal tradPrice;
	
	private BigDecimal shopPrice;
	
	private Integer isTrading;
	
	private Integer isDuration;
	
	private Integer durationDays;
	
	
	private Integer type;
	
	//位置
	private Integer positon;
	
	private String icon;
	
	private Integer status;
	
	private Date createTime;
	
	private Date updateTime;

	private String getWay;

	private BigDecimal magicPrice;

	private BigDecimal shopMagicPrice;

	private BigDecimal fund;

	private BigDecimal contribution;

	private Integer power;

	public BigDecimal getMagicPrice() {
		return magicPrice;
	}

	public void setMagicPrice(BigDecimal magicPrice) {
		this.magicPrice = magicPrice;
	}

	public BigDecimal getShopMagicPrice() {
		return shopMagicPrice;
	}

	public void setShopMagicPrice(BigDecimal shopMagicPrice) {
		this.shopMagicPrice = shopMagicPrice;
	}

	public String getGetWay() {
		return getWay;
	}

	public void setGetWay(String getWay) {
		this.getWay = getWay;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getPositon() {
		return positon;
	}

	public void setPositon(Integer positon) {
		this.positon = positon;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
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

	public Integer getIsSend() {
		return isSend;
	}

	public void setIsSend(Integer isSend) {
		this.isSend = isSend;
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


	public Integer getIsTrading() {
		return isTrading;
	}

	public void setIsTrading(Integer isTrading) {
		this.isTrading = isTrading;
	}

	public Integer getIsDuration() {
		return isDuration;
	}

	public void setIsDuration(Integer isDuration) {
		this.isDuration = isDuration;
	}

	public Integer getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(Integer durationDays) {
		this.durationDays = durationDays;
	}

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	public Integer getIsOverlap() {
		return isOverlap;
	}

	public void setIsOverlap(Integer isOverlap) {
		this.isOverlap = isOverlap;
	}

	public Integer getIsSell() {
		return isSell;
	}

	public void setIsSell(Integer isSell) {
		this.isSell = isSell;
	}

	public Integer getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(Integer currencyType) {
		this.currencyType = currencyType;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getTradPrice() {
		return tradPrice;
	}

	public void setTradPrice(BigDecimal tradPrice) {
		this.tradPrice = tradPrice;
	}
	
	public void setShopPrice(BigDecimal shopPrice) {
		this.shopPrice = shopPrice;
	}
	
	public BigDecimal getShopPrice() {
		return this.shopPrice;
	}


	public BigDecimal getFund() {
		return fund;
	}

	public void setFund(BigDecimal fund) {
		this.fund = fund;
	}

	public BigDecimal getContribution() {
		return contribution;
	}

	public void setContribution(BigDecimal contribution) {
		this.contribution = contribution;
	}

	public Integer getPower() {
		return power;
	}

	public void setPower(Integer power) {
		this.power = power;
	}

	public Integer getIsUse() {
		return isUse;
	}

	public void setIsUse(Integer isUse) {
		this.isUse = isUse;
	}

	public Integer getSynNumber() {
		return synNumber;
	}

	public void setSynNumber(Integer synNumber) {
		this.synNumber = synNumber;
	}


	public Integer getSynRate() {
		return synRate;
	}

	public void setSynRate(Integer synRate) {
		this.synRate = synRate;
	}

	public String getSynResultId() {
		return synResultId;
	}

	public void setSynResultId(String synResultId) {
		this.synResultId = synResultId;
	}

	public Integer getCanSyn() {
		return canSyn;
	}

	public void setCanSyn(Integer canSyn) {
		this.canSyn = canSyn;
	}

	public JSONArray getSynUse() {
		return synUse;
	}

	public void setSynUse(JSONArray synUse) {
		this.synUse = synUse;
	}

	public int getIsBook() {
		return isBook;
	}

	public void setIsBook(int isBook) {
		this.isBook = isBook;
	}

	public BigDecimal getBookReward() {
		return bookReward;
	}

	public void setBookReward(BigDecimal bookReward) {
		this.bookReward = bookReward;
	}
}
