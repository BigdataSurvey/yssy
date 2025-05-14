package com.zywl.app.defaultx.enmus;

/**
 *   每日类型
 * 
 * @author DOE
 *
 */
public enum DailyTaskTypeEnum {

	addCoin("看广告获取铜钱", 1),
	addPl("看广告增加PL", 2),
	prize("抽奖",3),
	elixir("加速",4),
	area("秘境",5),
	addMp("恢复mp",6);

	private String name;

	private int value;

	private DailyTaskTypeEnum(String name, int value) {
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
