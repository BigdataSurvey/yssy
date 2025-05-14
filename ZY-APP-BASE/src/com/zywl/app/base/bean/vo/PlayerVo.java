package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class PlayerVo extends BaseBean{
	
	
	private Long userId;
	
	private Integer lv;
	
	private Long exp;
	
	private Long nextLvExp;

	private BigDecimal buyCoin;
	private Integer pl;
	
	private String realm;
	
	private String skill;
	
	private int boss;
	
	private int mp;
	
	
	private int needTp;

	private int maxPl;

	public int getMaxPl() {
		return maxPl;
	}

	public void setMaxPl(int maxPl) {
		this.maxPl = maxPl;
	}

	public int getNeedTp() {
		return needTp;
	}

	public void setNeedTp(int needTp) {
		this.needTp = needTp;
	}

	public int getMp() {
		return mp;
	}

	public void setMp(int mp) {
		this.mp = mp;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getLv() {
		return lv;
	}

	public void setLv(Integer lv) {
		this.lv = lv;
	}

	

	public Long getExp() {
		return exp;
	}

	public void setExp(Long exp) {
		this.exp = exp;
	}

	public void setNextLvExp(Long nextLvExp) {
		this.nextLvExp = nextLvExp;
	}

	public Integer getPl() {
		return pl;
	}

	public void setPl(Integer pl) {
		this.pl = pl;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}

	public int getBoss() {
		return boss;
	}

	public void setBoss(int boss) {
		this.boss = boss;
	}

	public Long getNextLvExp() {
		return nextLvExp;
	}

	public BigDecimal getBuyCoin() {
		return buyCoin;
	}

	public void setBuyCoin(BigDecimal buyCoin) {
		this.buyCoin = buyCoin;
	}
}
