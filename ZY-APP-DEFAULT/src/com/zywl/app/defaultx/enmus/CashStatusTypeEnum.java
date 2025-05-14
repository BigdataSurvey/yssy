package com.zywl.app.defaultx.enmus;

/**
 *   提现类型
 * 
 * @author DOE
 *
 */
public enum CashStatusTypeEnum {

	UNAUDITED("未审核", 0),
	NO_SUBMIT("审核通过，等待推送",1),
	SUBMIT("已推送",2),
	SUCCESS("提现成功", 3),
	FAIL("提现失败",4);
	
	private String name;

	private int value;
	
	private CashStatusTypeEnum(String name, int value) {
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
