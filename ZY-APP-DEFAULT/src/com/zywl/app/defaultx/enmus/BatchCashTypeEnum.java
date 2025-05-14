package com.zywl.app.defaultx.enmus;

/**
 *   批量提现类型
 * 
 * @author DOE
 *
 */
public enum BatchCashTypeEnum {

	UNFINISHED("未完成", 1), 
	FINISHED("已完成", 2);
	
	private String name;

	private int value;
	
	private BatchCashTypeEnum(String name, int value) {
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
