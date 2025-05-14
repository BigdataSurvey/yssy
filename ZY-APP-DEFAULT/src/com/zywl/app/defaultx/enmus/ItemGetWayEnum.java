package com.zywl.app.defaultx.enmus;

/**
 *  道具来源
 * 
 * @author DOE
 *
 */
public enum ItemGetWayEnum {

	trading("交易行", 1), 
	shop("商店", 2),
	skill("技能掉落",3),
	elixir("炼丹",4);
	
	private String name;

	private int value;
	
	private ItemGetWayEnum(String name, int value) {
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
