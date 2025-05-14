package com.zywl.app.defaultx.enmus;

/**
 * 红点类型
 * **/
public enum RedPointTypeEnum {

    DAILY_TASK("每日任务","1101"),
    CARD_EQU("武将","CARD"),
    MAIL("邮件","501"),
    GET_CARD("抽卡","GET_CARD"),

    ATK_EQU("武器位置红点","905"),
    ADD_REWARD("获得奖励","ADD_REWARD");

    private RedPointTypeEnum(String name, String value) {
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
