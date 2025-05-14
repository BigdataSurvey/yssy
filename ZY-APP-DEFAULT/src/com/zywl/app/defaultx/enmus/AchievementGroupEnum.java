package com.zywl.app.defaultx.enmus;

/**
 *   成就类型
 * 
 * @author DOE
 *
 */
public enum AchievementGroupEnum {

	LOGIN("累计登录", 1),
	CHECKPOINT_NUMBER("通关主线任务", 2),
	GET_AWAIT_REWARD("领取挂机收益",3),
	LV_UP("达到等级", 4),
	SHOP_BUY("商店累计购买次数", 5),
	HAS_QUALITY_CARD_NUMBER("获取橙卡张数", 6),
	HAS_QUALITY_CARD_NUMBER2("获取橙卡张数", 6),
	CARD_UP_LV_NUMBER("武将升级次数", 7),
	CARD_SALVAGE_NUMBER("分解武将次数", 8),

	CHECK_POWER("战力", 10),
	DISPATCH_NUMBER("领取悬赏任务", 11),
	GET_PET_1("激活绝影",12),
	GET_PET_2("激活赤兔",13),
	GET_PET_3("激活的卢",14),
	GET_PET_4("激活大宛",15),
	GET_PET_5("激活爪黄飞电",16),
	GET_PET_6("激活惊帆",17),
	GET_PET_7("激活紫骍",18),
	GET_PET_8("激活白鹄",19),
	GET_CARD("招募武将",20),
	WEAR_SET("穿戴一套N阶装备",0),
	SYN_EQU_RANK("合成一套N阶装备",22),
	PVP_RANK("PVP名次",23),
	PVP_PK_COUNT("PVP累计获胜",24),
	TOWER_FLOOR("试炼之塔累计层数",25),
	OPEN_MINE("开通矿场个数",21),
	DAILY_TASK_AP_100("累计N天活跃度达到100点",26)
	;

	private String name;

	private int value;

	private AchievementGroupEnum(String name, int value) {
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
