package com.zywl.app.defaultx.enmus;

/**
 *  用户资产类型
 * 
 * @author DOE
 *
 */
public enum UserCapitalTypeEnum {

	currency_1("文币", 1),
	currency_2("通宝", 2),
	yyb("游园券",3),
	rmb("游园币", 4),

	;
	
	private String name;

	private int value;
	
	private UserCapitalTypeEnum(String name, int value) {
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
	
	public static String getName(int value) {
		UserCapitalTypeEnum[] ems = UserCapitalTypeEnum.values();
		for (UserCapitalTypeEnum userCapitalTypeEnum : ems) {
			if (userCapitalTypeEnum.getValue()==value) {
				return userCapitalTypeEnum.getName();
			}
		}
		return "资产";
	}
	
	
}
