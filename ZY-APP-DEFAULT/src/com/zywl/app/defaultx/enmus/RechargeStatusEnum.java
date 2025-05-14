package com.zywl.app.defaultx.enmus;

/**
 *   订单状态、
 * 
 * @author DOE
 *
 */
public enum RechargeStatusEnum {

	NO_PAY("未支付", 0),
	SUCCESS("支付成功", 1),
	FAIL("支付失败", 2),
	EXPIRE("支付超时", 3);

	private String name;

	private int value;

	private RechargeStatusEnum(String name, int value) {
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
