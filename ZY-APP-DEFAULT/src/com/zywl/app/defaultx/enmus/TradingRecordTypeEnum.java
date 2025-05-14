package com.zywl.app.defaultx.enmus;

/**
 *   交易行交易类型
 * 
 * @author DOE
 *
 */
public enum TradingRecordTypeEnum {

	buy("购买道具", 1), 
	sell("卖出道具", 2),
	askbuy("求购得到道具",3);
	private String name;

	private int value;
	
	private TradingRecordTypeEnum(String name, int value) {
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
