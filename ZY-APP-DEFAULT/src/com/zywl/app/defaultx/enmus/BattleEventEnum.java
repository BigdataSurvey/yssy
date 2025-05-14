package com.zywl.app.defaultx.enmus;

/**
 * 对战事件
 * **/
public enum BattleEventEnum {

    SKILL("技能","skill"),
    ATTACK("普攻","attack"),
    ROUND("回合开始","round"),

    COMBO("连击","combo"),

    RESULT("结算","result"),

    ZHILIAO("治疗","zhiliao")

    ;

    private BattleEventEnum(String name, String value) {
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
