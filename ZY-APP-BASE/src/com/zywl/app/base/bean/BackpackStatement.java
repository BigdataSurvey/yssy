package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.io.Serializable;


/**
 * 
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-12-01 11:48:06
 */
public class BackpackStatement extends BaseBean {

	//
	private Integer id;
	//
	private String ymd;
	//
	private Long itemId;
	//
	private Integer use=0;
	//
	private Integer addShop=0;
	//
	private Integer addSign=0;
	//
	private Integer addPrize=0;
	//
	private Integer addElixir=0;
	//
	private Integer addSkill=0;
	//
	private Integer addAchievement=0;
	//
	private Integer addEvent=0;
	//
	private Integer tradListing=0;
	//
	private Integer tradDesting=0;
	//
	private Integer tradAskBuy=0;
	//
	//
	private Integer tradSell=0;
	//
	private Integer tradBuy=0;

	/**
	 * 设置：
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * 获取：
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * 设置：
	 */
	public void setYmd(String ymd) {
		this.ymd = ymd;
	}
	/**
	 * 获取：
	 */
	public String getYmd() {
		return ymd;
	}
	/**
	 * 设置：
	 */
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	/**
	 * 获取：
	 */
	public Long getItemId() {
		return itemId;
	}
	/**
	 * 设置：
	 */
	public void setUse(Integer use) {
		this.use = use;
	}
	/**
	 * 获取：
	 */
	public Integer getUse() {
		return use;
	}
	/**
	 * 设置：
	 */
	public void setAddShop(Integer addShop) {
		this.addShop = addShop;
	}
	/**
	 * 获取：
	 */
	public Integer getAddShop() {
		return addShop;
	}
	/**
	 * 设置：
	 */
	public void setAddSign(Integer addSign) {
		this.addSign = addSign;
	}
	/**
	 * 获取：
	 */
	public Integer getAddSign() {
		return addSign;
	}
	/**
	 * 设置：
	 */
	public void setAddPrize(Integer addPrize) {
		this.addPrize = addPrize;
	}
	/**
	 * 获取：
	 */
	public Integer getAddPrize() {
		return addPrize;
	}
	/**
	 * 设置：
	 */
	public void setAddElixir(Integer addElixir) {
		this.addElixir = addElixir;
	}
	/**
	 * 获取：
	 */
	public Integer getAddElixir() {
		return addElixir;
	}
	/**
	 * 设置：
	 */
	public void setAddSkill(Integer addSkill) {
		this.addSkill = addSkill;
	}
	/**
	 * 获取：
	 */
	public Integer getAddSkill() {
		return addSkill;
	}
	/**
	 * 设置：
	 */
	public void setAddAchievement(Integer addAchievement) {
		this.addAchievement = addAchievement;
	}
	/**
	 * 获取：
	 */
	public Integer getAddAchievement() {
		return addAchievement;
	}
	/**
	 * 设置：
	 */
	public void setAddEvent(Integer addEvent) {
		this.addEvent = addEvent;
	}
	/**
	 * 获取：
	 */
	public Integer getAddEvent() {
		return addEvent;
	}
	/**
	 * 设置：
	 */
	public void setTradListing(Integer tradListing) {
		this.tradListing = tradListing;
	}
	/**
	 * 获取：
	 */
	public Integer getTradListing() {
		return tradListing;
	}
	/**
	 * 设置：
	 */
	public void setTradDesting(Integer tradDesting) {
		this.tradDesting = tradDesting;
	}
	/**
	 * 获取：
	 */
	public Integer getTradDesting() {
		return tradDesting;
	}
	/**
	 * 设置：
	 */
	public void setTradAskBuy(Integer tradAskBuy) {
		this.tradAskBuy = tradAskBuy;
	}
	/**
	 * 获取：
	 */
	public Integer getTradAskBuy() {
		return tradAskBuy;
	}
	/**
	 * 设置：
	 */
	public void setTradSell(Integer tradSell) {
		this.tradSell = tradSell;
	}
	/**
	 * 获取：
	 */
	public Integer getTradSell() {
		return tradSell;
	}
	/**
	 * 设置：
	 */
	public void setTradBuy(Integer tradBuy) {
		this.tradBuy = tradBuy;
	}
	/**
	 * 获取：
	 */
	public Integer getTradBuy() {
		return tradBuy;
	}
}
