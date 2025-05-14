package com.zywl.app.defaultx.enmus;

/**
 *   商店类型
 * 
 * @author DOE
 *
 */
public enum ShopTypeEnum {

	YUANBAO("元宝商店", 4),
	PVP("演武场商店", 5),
	PET("战马商城", 501),
	FRIEND("友情商店", 7);

	private String name;

	private int value;

	private ShopTypeEnum(String name, int value) {
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
