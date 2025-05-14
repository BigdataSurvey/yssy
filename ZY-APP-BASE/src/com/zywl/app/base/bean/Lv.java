package com.zywl.app.base.bean;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class Lv extends BaseBean{
	
	private int lv;
	
	private String name;

	private BigDecimal needCoin;
	private Long needExp;

	private Long characterExp;
	private JSONArray getItem;
	
	private String needItem;
	
	private BigDecimal dropCoin;
	
	private BigDecimal adCoin;
	
	private BigDecimal offlineCoin;



	private BigDecimal buyCoin;

	private int maxPl;

	private Long power;

	public int getMaxPl() {
		return maxPl;
	}

	public void setMaxPl(int maxPl) {
		this.maxPl = maxPl;
	}

	public BigDecimal getOfflineCoin() {
		return offlineCoin;
	}

	public void setOfflineCoin(BigDecimal offlineCoin) {
		this.offlineCoin = offlineCoin;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getNeedExp() {
		return needExp;
	}

	public void setNeedExp(Long needExp) {
		this.needExp = needExp;
	}


	public String getNeedItem() {
		return needItem;
	}

	public void setNeedItem(String needItem) {
		this.needItem = needItem;
	}

	public BigDecimal getDropCoin() {
		return dropCoin;
	}

	public void setDropCoin(BigDecimal dropCoin) {
		this.dropCoin = dropCoin;
	}

	public BigDecimal getAdCoin() {
		return adCoin;
	}

	public void setAdCoin(BigDecimal adCoin) {
		this.adCoin = adCoin;
	}

	public BigDecimal getBuyCoin() {
		return buyCoin;
	}

	public void setBuyCoin(BigDecimal buyCoin) {
		this.buyCoin = buyCoin;
	}

	public Long getPower() {
		return power;
	}

	public void setPower(Long power) {
		this.power = power;
	}

	public BigDecimal getNeedCoin() {
		return needCoin;
	}

	public void setNeedCoin(BigDecimal needCoin) {
		this.needCoin = needCoin;
	}

	public JSONArray getGetItem() {
		return getItem;
	}

	public void setGetItem(JSONArray getItem) {
		this.getItem = getItem;
	}

	public Long getCharacterExp() {
		return characterExp;
	}

	public void setCharacterExp(Long characterExp) {
		this.characterExp = characterExp;
	}
}
