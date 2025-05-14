package com.zywl.app.defaultx.enmus;

/**
 *   收益类型
 * 
 * @author DOE
 *
 */
public enum IncomeTypeEnum {

	advertisement("广告收益", 1), 
	playGame("试玩收益",2),
	user_register("用户注册",3);
	private String name;

	private int value;
	
	private IncomeTypeEnum(String name, int value) {
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
