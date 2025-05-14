package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.io.Serializable;
import java.util.Date;




public class XianWanOrder extends BaseBean {
	private static final long serialVersionUID = 1L;
	
	//
	private Long id;
	//
	private Integer adid;
	//
	private String adname;
	//
	private String appid;
	//
	private String ordernum;
	//
	private Integer dlevel;
	//
	private String pagename;
	//
	private Integer atype;
	//
	private String deviceid;
	//
	private String simid;
	//
	private String appsign;
	//
	private String merid;
	//
	private String event;
	//
	private String adicon;
	//
	private String price;
	//
	private String money;
	//
	private Date itime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 设置：
	 */
	public void setAdid(Integer adid) {
		this.adid = adid;
	}
	/**
	 * 获取：
	 */
	public Integer getAdid() {
		return adid;
	}
	/**
	 * 设置：
	 */
	public void setAdname(String adname) {
		this.adname = adname;
	}
	/**
	 * 获取：
	 */
	public String getAdname() {
		return adname;
	}
	/**
	 * 设置：
	 */
	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	/**
	 * 设置：
	 */
	public void setOrdernum(String ordernum) {
		this.ordernum = ordernum;
	}
	/**
	 * 获取：
	 */
	public String getOrdernum() {
		return ordernum;
	}
	/**
	 * 设置：
	 */
	public Integer getDlevel() {
		return dlevel;
	}

	public void setDlevel(Integer dlevel) {
		this.dlevel = dlevel;
	}

	/**
	 * 设置：
	 */
	public void setPagename(String pagename) {
		this.pagename = pagename;
	}
	/**
	 * 获取：
	 */
	public String getPagename() {
		return pagename;
	}
	/**
	 * 设置：
	 */
	public void setAtype(Integer atype) {
		this.atype = atype;
	}
	/**
	 * 获取：
	 */
	public Integer getAtype() {
		return atype;
	}
	/**
	 * 设置：
	 */
	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}
	/**
	 * 获取：
	 */
	public String getDeviceid() {
		return deviceid;
	}
	/**
	 * 设置：
	 */
	public String getSimid() {
		return simid;
	}

	public void setSimid(String simid) {
		this.simid = simid;
	}

	/**
	 * 设置：
	 */
	public void setAppsign(String appsign) {
		this.appsign = appsign;
	}
	/**
	 * 获取：
	 */
	public String getAppsign() {
		return appsign;
	}
	/**
	 * 设置：
	 */
	public void setMerid(String merid) {
		this.merid = merid;
	}
	/**
	 * 获取：
	 */
	public String getMerid() {
		return merid;
	}
	/**
	 * 设置：
	 */
	public void setEvent(String event) {
		this.event = event;
	}
	/**
	 * 获取：
	 */
	public String getEvent() {
		return event;
	}
	/**
	 * 设置：
	 */
	public void setAdicon(String adicon) {
		this.adicon = adicon;
	}
	/**
	 * 获取：
	 */
	public String getAdicon() {
		return adicon;
	}
	/**
	 * 设置：
	 */
	public void setPrice(String price) {
		this.price = price;
	}
	/**
	 * 获取：
	 */
	public String getPrice() {
		return price;
	}
	/**
	 * 设置：
	 */
	public void setMoney(String money) {
		this.money = money;
	}
	/**
	 * 获取：
	 */
	public String getMoney() {
		return money;
	}
	/**
	 * 设置：
	 */
	public void setItime(Date itime) {
		this.itime = itime;
	}
	/**
	 * 获取：
	 */
	public Date getItime() {
		return itime;
	}
}
