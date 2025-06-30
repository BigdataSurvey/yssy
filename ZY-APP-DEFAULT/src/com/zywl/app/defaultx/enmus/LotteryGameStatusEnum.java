package com.zywl.app.defaultx.enmus;

/**
 *   大逃杀游戏状态
 * 
 * @author DOE
 *
 */
public enum LotteryGameStatusEnum {

	cantPlay("禁止游戏状态",0),
	ready("准备阶段", 1), 
	gaming("下注阶段", 2),
	settle("结算阶段",3),
	alda("已经击打",4);

	private String name;

	private int value;
	
	private LotteryGameStatusEnum(String name, int value) {
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
