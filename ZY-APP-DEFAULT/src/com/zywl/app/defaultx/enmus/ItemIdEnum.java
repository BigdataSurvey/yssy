package com.zywl.app.defaultx.enmus;

/**
 * 道具ID配置
 **/
public enum ItemIdEnum {

    PVP_ITEM("挑战令", "3"),
    MONEY_1("银币", "1"),
    FRIEND("友情点", "7"),
    GOLD("金币", "2"),
    CARD_EXP("卡牌经验", "2"),
    PVP_SCORE("PVP积分", "5"),
    YUANBAO("金币", "4"),
    AP("活跃度", "6"),
    GET_CARD_1("战将招募令", "202"),
    GET_CARD_2("友情招募令", "203"),
    GET_CARD_3("名将招募令", "204"),
    PET_SHOP_USE("战马商城所用", "501"),
    CARD_WEI_5("5星魏国碎片", "206"),
    CARD_SHU_5("5星蜀国碎片", "207"),
    CARD_WU_5("5星吴国碎片", "208"),
    CARD_QUN_5("5星群雄碎片", "205"),
    CARD_WEI_4("4星魏国碎片", "210"),
    CARD_SHU_4("4星蜀国碎片", "211"),
    CARD_WU_4("4星吴国碎片", "212"),
    CARD_QUN_4("4星群雄碎片", "209"),
    CARD_SUIJI_4("4星随机碎片", "214"),
    CARD_SUIJI_5("5星随机碎片", "215"),
    PET_JUEYING("绝影碎片", "801"),
    PET_CHITU("赤兔碎片", "803"),
    PET_DILU("的卢碎片", "804"),
    PET_DAWAN("大宛碎片", "805"),
    PET_ZHFD("爪黄飞电碎片", "810"),
    PET_JINGFAN("惊帆碎片", "808"),
    PET_ZIXIN("紫骍碎片", "818"),
    PET_BAIHAO("白鹄碎片", "809"),
    PET_QINGLONG("青龙碎片", "705"),
    PET_BAIHU("白虎碎片", "706"),
    PET_ZHUQUE("朱雀碎片", "707"),
    PET_XUANWU("玄武碎片", "708"),
    CY1_PACK_1("银币礼包（小）", "301"),
    CY1_PACK_2("银币礼包（中）", "302"),
    CY1_PACK_3("银币礼包（大）", "303"),
    CARD_EXP_1("武将经验礼包（小）", "304"),
    CARD_EXP_2("武将经验礼包（中）", "305"),
    CARD_EXP_3("武将经验礼包（大）", "306"),
    DICE("骰子", "523"),
    BUY_PET("龙驹玉","501"),
    TREASURE_MAP_1("普通藏宝图", "120"),
    TREASURE_MAP_2("高级藏宝图","121"),
    PET_LV_NEED("兽魂","522")
    ;


    private ItemIdEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
