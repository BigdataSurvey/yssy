package com.zywl.app.defaultx.enmus;

/**
 *   提现类型
 *
 * @author DOE
 *
 */
public enum TopTypeEnum {

	POPULAR("人气排行榜", 3),
	VIP("vip", 2),
	INVITE("邀请榜",1),
	TOWER_TOP("试炼之塔",4),
	POWER("战力排行榜",5),
	CHECK_POINT("友情值排行榜",6),
	CONSUME("消耗排行榜",7);

	private String name;

	private int value;

	private TopTypeEnum(String name, int value) {
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
