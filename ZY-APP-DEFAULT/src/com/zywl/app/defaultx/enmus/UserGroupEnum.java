package com.zywl.app.defaultx.enmus;

/**
 *   提现类型
 * 
 * @author DOE
 *
 */
public enum UserGroupEnum {

	NORMAL_USER("正常用户", 1), 
	UN_WITHDRAWAL_USER("禁止提现用户", 2),
	RISK_USER("风控用户",3);
	
	private String name;

	private int value;
	
	private UserGroupEnum(String name, int value) {
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
