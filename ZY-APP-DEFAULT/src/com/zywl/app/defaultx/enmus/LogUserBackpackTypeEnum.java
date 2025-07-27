package com.zywl.app.defaultx.enmus;

/**
 *  用户背包变动类型
 * 
 * @author DOE
 *
 */
public enum LogUserBackpackTypeEnum {
	test("测试",0),
	use("使用", 1), 
	listing("上架", 2), 
	delist("下架", 3),
	events("活动获取",4),
	buy("交易行购买",5),
	sell("交易行卖出到求购",6),
	askbuy("交易行求购得到道具",7),
	mail("邮件领取",8),
	sign_reward("签到奖励",9),
	achievement_reward("成就奖励",10),
	skill("技能掉落",11),
	receive_total_sign_reward("累签奖励",12),
	elixir("炼丹房炼制",13),
	prize_draw("转盘抽奖",14),
	pet("合成/升级 灵兽",15),
	add_pet_pl("灵兽疲劳恢复",16),
	receice_pet("领取灵蛋",17),
	buff("合成/升级 底座",18),
	sell_sys("背包物品出售",19),
	shopping("商城购买",20),
	stolen("偷蛋",21),
	tree("灵气树果子",22),
	daily_task("每日任务",23),
	cave_prize_draw("开启宝箱",24),
	search("藏宝图搜寻",25),
	xm_buy("门派商店购买",26),
	ancient_get("遗迹获得",27),
	checkpoint("主线过关奖励",28),
	tower("试炼之塔",29),
	game("游戏获得",30),
	open_mine("开通矿产",1002),
	treasure_map_get("藏宝图获得",1003),
	yy("游园使用",1004),
	addHp("补充体力", 1005),
	jz("捐赠", 1005),
	zsg("赠送给",1006),
	zs("赠送",1007),

	syn("合成获得",1008),
	buy_pit("开通矿洞",1008),
	receive_number("矿洞收益",1010),
	pit_refund("矿洞退款",1009)


	;
	
	private String name;

	private int value;
	
	private LogUserBackpackTypeEnum(String name, int value) {
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

	public void setHour(int value) {
		this.value = value;
	}
}
