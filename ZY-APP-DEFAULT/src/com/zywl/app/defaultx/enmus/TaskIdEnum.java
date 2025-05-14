package com.zywl.app.defaultx.enmus;

/**
 * 副本类型枚举
 * **/
public enum TaskIdEnum {

    LOGIN("每日登录","1"),
    GET_CARD("进行5次招募","2"),
    AWAIT("快速作战三次","3"),
    DAILY_FB("通过或扫荡3次日常副本","4"),
    TOWER("通关或扫荡3次试炼之塔","5"),
    GET_AWAIT_REWARD_COUNT("领取挂机收益1次","6"),
    SHOP_BUY_NUMBER("任意商城购买1次","7"),
    DISPATCH_NUMBER("完成悬赏任务3次","7"),
    PVP_PK_COUNT("演武场挑战3次","8"),
    TOP_LIKE_NUMBER("排行榜点赞次数", "9"),

    ;

    private TaskIdEnum(String name, String value) {
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
