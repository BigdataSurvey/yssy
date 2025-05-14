package com.zywl.app.defaultx.enmus;

/**
 *   提现类型
 * 
 * @author DOE
 *
 */
public enum CashTypeEnum {

	WX("微信提现", 1), 
	ZFB("支付宝提现", 2);
	
	private String name;

	private int value;
	
	private CashTypeEnum(String name, int value) {
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
