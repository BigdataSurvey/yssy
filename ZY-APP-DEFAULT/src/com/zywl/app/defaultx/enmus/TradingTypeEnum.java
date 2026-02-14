package com.zywl.app.defaultx.enmus;

/**
 *   交易行类型
 */
public enum TradingTypeEnum {

	sell("交易商城", 0),
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


	public int getValue() {
		return value;
	}




}
