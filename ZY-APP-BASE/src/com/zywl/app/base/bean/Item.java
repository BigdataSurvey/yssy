package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;
/**
 * @Author: lzx
 * @Create: 2025/12/1
 * @Version: V1.0
 * @Task: 道具表对象
 */
public class Item extends BaseBean{

	/** 主键ID */
	private Long id;

	/** 道具名称 */
	private String name;

	/** 道具数量 */
	private Integer number;

	/** 描述 */
	private String context;

	/** 是否是书 */
	private int isBook;

	/** 书的奖励 */
	private BigDecimal bookReward;

	/** 品质（1白 2绿 3蓝 4紫 5橙） */
	private Integer quality;

	/** 可使用（1=可用，0=不可用） */
	private Integer isUse;

	/** 是否可叠加（1=可叠加，0=不可叠加） */
	private Integer isOverlap;

	/** 是否可出售（1=可出售，0=不可出售） */
	private Integer isSell;

	/** 是否可赠送（1=可赠送，0=不可赠送） */
	private Integer isSend;

	/** 同步数量 */
	private Integer synNumber;

	/** 同步可否（1=可同步，0=不可同步） */
	private Integer canSyn;

	/** 同步使用 */
	private JSONArray synUse;

	/** 同步概率 */
	private Integer synRate;

	/** 同步结果ID */
	private String synResultId;


	private Integer currencyType;

	/** 出售价格 */
	private BigDecimal price;

	/** 交易价格 */
	private BigDecimal tradPrice;

	/** 售价价格 */
	private BigDecimal shopPrice;

	/** 可交易（1=可以，0=不可以） */
	private Integer isTrading;

	/** 是否有冷却/持续时间 */
	private Integer isDuration;

	/** 持续时间 */
	private Integer durationDays;

	/** 类型 1材料 / 2种子&基础道具 / 3功能道具 / 4货币 / 5礼包(预留) */
	private Integer type;

	/** 客户端展示位置 1货币栏 / 2道具背包 */
	private Integer positon;

	/** 图标资源名 */
	private String icon;

	/** 状态（1=有效，0=下架） */
	private Integer status;

	/** 创建时间 */
	private Date createTime;
	/** 更新时间 */
	private Date updateTime;
	/** 获取途径 */
	private String getWay;
	/** 魔法值价格 */
	private BigDecimal magicPrice;
	/** 商城魔法值价格 */
	private BigDecimal shopMagicPrice;
	/** 仙门基金价格 */
	private BigDecimal fund;
	/** 需要的仙门贡献值 */
	private BigDecimal contribution;
	/** 效果数值（果实类道具恢复体力量） */
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
