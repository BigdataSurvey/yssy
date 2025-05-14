package com.zywl.app.defaultx.enmus;

/**
 * 对战类型
 * **/
public enum BattleTypeEnum {

    CHECKPOINT("主线",1),
    PVP("演武场",2),
    TOWER("试炼之塔",3),
    EVERY_DAY_FB("日常副本",4);

    private BattleTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private int value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
