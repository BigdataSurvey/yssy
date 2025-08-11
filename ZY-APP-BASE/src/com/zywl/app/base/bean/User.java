package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class User extends BaseBean{
	
	private Long id;
	
	private String userNo;
	
	private int firstCharge;

	private Long parentId;
	
	private Long grandfaId;

	private int isChannel;

	private Integer isChannel2;

	private String channelNo;
	
	private String inviteCode;

	private String oldInviteCode;

	private Integer roleId;

	private String name;
	
	private Integer nameStatus;

	private int vip1;

	private Date vipExpireTime;

	private int vip2;

	private Date vip2ExpireTime;

	private String tabtabId;

	private Integer authentication;
	
	private String phone;
	
	private Integer sex;
	
	private String mail;
	
	private String account;
	
	private String password;
	
	private String wechatId;
	
	private String qq;
	
	private String idCard;

	private int isUpdateIdCard;
	
	private String province;
	
	private String city;
	
	private String realName;
	
	private String courierName;
	
	private String courierPhone;
	
	private String courierAddress;

	private int isCash;
	
	private String openId;
	
	private String unionId;

	private String gameToken;

	private Date tokenTime;


	
	private Date registTime;
	
	private Date lastLoginTime;
	
	private Date lastLeaveTime;
	
	private String registIp;

	private String lastLoginIp;
	
	private String headImageUrl;
	
	private Integer status;
	
	private Integer group;

	private String alipayId;
	
	private BigDecimal points;
	
	private String parentTree;
	
	private Integer loginTimes;

	private String cno;

	private Integer risk;
	private Integer riskPlus;

	private Integer isBot;

	public Integer getIsBot() {
		return isBot;
	}

	public void setIsBot(Integer isBot) {
		this.isBot = isBot;
	}

	public Integer getRiskPlus() {
		return riskPlus;
	}

	public void setRiskPlus(Integer riskPlus) {
		this.riskPlus = riskPlus;
	}

	public int getFirstCharge() {
		return firstCharge;
	}

	public void setFirstCharge(int firstCharge) {
		this.firstCharge = firstCharge;
	}


	public Long getId() {
		return id;
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


	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public Integer getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Integer authentication) {
		this.authentication = authentication;
	}


	public String getTabtabId() {
		return tabtabId;
	}

	public void setTabtabId(String tabtabId) {
		this.tabtabId = tabtabId;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Long getGrandfaId() {
		return grandfaId;
	}

	public void setGrandfaId(Long grandfaId) {
		this.grandfaId = grandfaId;
	}

	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	
	public Integer getNameStatus() {
		return nameStatus;
	}

	public void setNameStatus(Integer nameStatus) {
		this.nameStatus = nameStatus;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
	
	public String getWechatId() {
		return wechatId==null?"":wechatId;
	}

	public void setWechatId(String wechatId) {
		this.wechatId = wechatId;
	}

	public String getQq() {
		return qq==null?"":qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	
	
	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}
	
	

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	public Date getRegistTime() {
		return registTime;
	}

	public void setRegistTime(Date registTime) {
		this.registTime = registTime;
	}

	

	


	public String getRegistIp() {
		return registIp;
	}

	public void setRegistIp(String registIp) {
		this.registIp = registIp;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public String getHeadImageUrl() {
		return headImageUrl;
	}

	public void setHeadImageUrl(String headImageUrl) {
		this.headImageUrl = headImageUrl;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getGroup() {
		return group;
	}

	public void setGroup(Integer group) {
		this.group = group;
	}

	public String getParentTree() {
		return parentTree;
	}

	public void setParentTree(String parentTree) {
		this.parentTree = parentTree;
	}

	

	public Integer getLoginTimes() {
		return loginTimes;
	}

	public void setLoginTimes(Integer loginTimes) {
		this.loginTimes = loginTimes;
	}

	public Date getLastLeaveTime() {
		return lastLeaveTime;
	}

	public void setLastLeaveTime(Date lastLeaveTime) {
		this.lastLeaveTime = lastLeaveTime;
	}

	public BigDecimal getPoints() {
		return points;
	}

	public void setPoints(BigDecimal points) {
		this.points = points;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Integer getRisk() {
		return risk;
	}

	public void setRisk(Integer risk) {
		this.risk = risk;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public int getIsChannel() {
		return isChannel;
	}

	public void setIsChannel(int isChannel) {
		this.isChannel = isChannel;
	}

	public Date getVipExpireTime() {
		return vipExpireTime;
	}

	public void setVipExpireTime(Date vipExpireTime) {
		this.vipExpireTime = vipExpireTime;
	}

	public int getIsCash() {
		return isCash;
	}

	public void setIsCash(int isCash) {
		this.isCash = isCash;
	}

	public String getAlipayId() {
		return alipayId;
	}

	public Integer getIsChannel2() {
		return isChannel2;
	}

	public void setIsChannel2(Integer isChannel2) {
		this.isChannel2 = isChannel2;
	}

	public void setAlipayId(String alipayId) {
		this.alipayId = alipayId;
	}

	public int getVip1() {
		return vip1;
	}

	public void setVip1(int vip1) {
		this.vip1 = vip1;
	}

	public int getVip2() {
		return vip2;
	}

	public void setVip2(int vip2) {
		this.vip2 = vip2;
	}

	public Date getVip2ExpireTime() {
		return vip2ExpireTime;
	}

	public void setVip2ExpireTime(Date vip2ExpireTime) {
		this.vip2ExpireTime = vip2ExpireTime;
	}

	public String getGameToken() {
		return gameToken;
	}

	public void setGameToken(String gameToken) {
		this.gameToken = gameToken;
	}

	public Date getTokenTime() {
		return tokenTime;
	}

	public void setTokenTime(Date tokenTime) {
		this.tokenTime = tokenTime;
	}

	public int getIsUpdateIdCard() {
		return isUpdateIdCard;
	}

	public void setIsUpdateIdCard(int isUpdateIdCard) {
		this.isUpdateIdCard = isUpdateIdCard;
	}

	public String getCno() {
		return cno;
	}

	public void setCno(String cno) {
		this.cno = cno;
	}

	public String getOldInviteCode() {
		return oldInviteCode;
	}

	public void setOldInviteCode(String oldInviteCode) {
		this.oldInviteCode = oldInviteCode;
	}
}
