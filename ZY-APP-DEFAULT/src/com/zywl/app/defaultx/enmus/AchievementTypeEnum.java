package com.zywl.app.defaultx.enmus;

/**
 *   成就类型
 * 
 * @author DOE
 *
 */
public enum AchievementTypeEnum {

	lv("升级", 7),
	bb("连续看广告", 8),
	zdl("邀请好友",9);
	
	private String name;

	private int value;
	
	private AchievementTypeEnum(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValie(int value) {
		this.value = value;
	}
	
	
}
