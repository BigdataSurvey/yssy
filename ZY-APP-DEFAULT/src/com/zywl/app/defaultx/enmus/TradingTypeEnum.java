package com.zywl.app.defaultx.enmus;

/**
 *   交易行类型
 * 
 * @author DOE
 *
 */
public enum TradingTypeEnum {

	sell("道具出售", 0), 
	askbuy("道具求购", 1);
	
	private String name;

	private int value;
	
	private TradingTypeEnum(String name, int value) {
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
