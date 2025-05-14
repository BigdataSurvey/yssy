package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class UserVo extends BaseBean{
	
	private Long id;
	
	private String userNo;

	private Long parentId;
	private Integer roleId;
	private String name;

	private String idCard;
	private String headImageUrl;

	private int isCash;
	private String realName;

	private String phone;

	private Integer vip1;

	private Date vipExpireTime;

	private Integer vip2;

	private Date vip2ExpireTime;

	private Integer authentication;
	
	private String qq;

	private Integer risk;
	
	private String wechatId;

	private String alipayId;

	private String courierName;

	private String courierPhone;

	private String courierAddress;

	private String gameToken;

	public String getCourierName() {
		return courierName;
	}

	public void setCourierName(String courierName) {
		this.courierName = courierName;
	}

	public String getCourierPhone() {
		return courierPhone;
	}

	public void setCourierPhone(String courierPhone) {
		this.courierPhone = courierPhone;
	}

	public String getCourierAddress() {
		return courierAddress;
	}

	public void setCourierAddress(String courierAddress) {
		this.courierAddress = courierAddress;
	}

	public Integer getVip1() {
		return vip1;
	}

	public void setVip1(Integer vip1) {
		this.vip1 = vip1;
	}

	public Integer getVip2() {
		return vip2;
	}

	public void setVip2(Integer vip2) {
		this.vip2 = vip2;
	}

	public Date getVip2ExpireTime() {
		return vip2ExpireTime;
	}

	public void setVip2ExpireTime(Date vip2ExpireTime) {
		this.vip2ExpireTime = vip2ExpireTime;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Integer getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Integer authentication) {
		this.authentication = authentication;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getWechatId() {
		return wechatId;
	}

	public void setWechatId(String wechatId) {
		this.wechatId = wechatId;
	}

	public Long getId() {
		return id;
	}

	public int getIsCash() {
		return isCash;
	}

	public void setIsCash(int isCash) {
		this.isCash = isCash;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHeadImageUrl() {
		return headImageUrl;
	}

	public void setHeadImageUrl(String headImageUrl) {
		this.headImageUrl = headImageUrl;
	}


	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public Date getVipExpireTime() {
		return vipExpireTime;
	}

	public void setVipExpireTime(Date vipExpireTime) {
		this.vipExpireTime = vipExpireTime;
	}

	public Integer getRisk() {
		return risk;
	}

	public void setRisk(Integer risk) {
		this.risk = risk;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAlipayId() {
		return alipayId;
	}

	public void setAlipayId(String alipayId) {
		this.alipayId = alipayId;
	}

	public String getGameToken() {
		return gameToken;
	}

	public void setGameToken(String gameToken) {
		this.gameToken = gameToken;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
}
