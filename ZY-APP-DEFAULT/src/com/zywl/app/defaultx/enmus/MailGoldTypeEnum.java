package com.zywl.app.defaultx.enmus;

/**
 *   收益类型
 * 
 * @author DOE
 *
 */
public enum MailGoldTypeEnum {

	TRADING("拍卖行", 1),
	PLAY_GAME("试玩收益",2),
	GAME("游戏奖励",3),
	FRIEND_PLAY_GAME("试玩收益",4),
	FRIEND("好友赠送",4);
	private String name;

	private int value;

	private MailGoldTypeEnum(String name, int value) {
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
