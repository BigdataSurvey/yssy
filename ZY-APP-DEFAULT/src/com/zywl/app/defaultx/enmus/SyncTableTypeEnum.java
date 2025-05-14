package com.zywl.app.defaultx.enmus;

/**
 *   同步表的类型
 * 
 * @author 1
 *
 */
public enum SyncTableTypeEnum {

	item("道具表", 1);
	private String name;

	private int value;
	
	private SyncTableTypeEnum(String name, int value) {
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
