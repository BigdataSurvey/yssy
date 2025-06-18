package com.zywl.app.defaultx.enmus;

/**
 *   限时活动累计积分类型
 * 
 * @author DOE
 *
 */
public enum ActivityAddPointEventEnum {

	RMB_BUY_GIFT("直冲购买礼包", 1),
	GAME_MONEY_BUY_GIFT("通宝购买礼包", 2),


	;

	private String name;

	private int value;

	private ActivityAddPointEventEnum(String name, int value) {
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
