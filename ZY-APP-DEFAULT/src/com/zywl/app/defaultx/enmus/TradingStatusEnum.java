package com.zywl.app.defaultx.enmus;

/**
 *   交易行状态类型
 * 
 * @author DOE
 *
 */
public enum TradingStatusEnum {

	listing("上架中", 1), 
	unlisting("已取消", 2),
	finsh("已完成",0);
	
	private String name;

	private int value;
	
	private TradingStatusEnum(String name, int value) {
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
