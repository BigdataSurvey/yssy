package com.zywl.app.defaultx.enmus;
import lombok.Getter;
/**
 * 道具ID配置
 **/
@Getter
public enum ItemIdEnum {
    MONEY_1("银币", "1"),
    GOLD("金币", "2"),
    XLB("小喇叭","40"),
    DLB("大喇叭", "12"),
    BUY_WHITE("购买凭证","43"),
    CORE_POINT("核心积分", "1001"),
    GAME_CONSUME_COIN("游戏消耗货币", "1002"),
    SEED_CORN_LV1("玉米种子·一阶", "1101"),
    SEED_CORN_LV2("玉米种子·二阶", "1102"),
    SEED_CORN_LV3("玉米种子·三阶", "1103"),
    SEED_CORN_LV4("玉米种子·四阶", "1104"),
    SEED_CORN_LV5("玉米种子·五阶", "1105"),
    SEED_WHEAT_LV1("小麦种子·一阶", "1201"),
    SEED_WHEAT_LV2("小麦种子·二阶", "1202"),
    SEED_WHEAT_LV3("小麦种子·三阶", "1203"),
    SEED_WHEAT_LV4("小麦种子·四阶", "1204"),
    SEED_WHEAT_LV5("小麦种子·五阶", "1205"),
    SEED_RADISH_LV1("萝卜种子·一阶", "1301"),
    SEED_RADISH_LV2("萝卜种子·二阶", "1302"),
    SEED_RADISH_LV3("萝卜种子·三阶", "1303"),
    SEED_RADISH_LV4("萝卜种子·四阶", "1304"),
    SEED_RADISH_LV5("萝卜种子·五阶", "1305"),
    MATERIAL_CORN("玉米", "2101"),
    MATERIAL_WHEAT("小麦", "2102"),
    MATERIAL_RADISH("萝卜", "2103"),

    BALLOON("气球", "2105"),
    VOUCHER_GAME_COIN("游戏货币兑换券", "3001"),
    GIFT_CONTRIBUTION("贡献奖励礼包", "3002"),
    ;

    private ItemIdEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
